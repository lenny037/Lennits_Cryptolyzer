// MODULE 19: lennit-genesis-core — JNI bridge for Android NPU acceleration
// Target: Samsung S20 FE (Snapdragon 865 / Hexagon 698 DSP)
//
// Safety contract:
//   • All exported functions are #[no_mangle] + pub extern "system"
//   • No .unwrap() / .expect() — all errors throw Java exceptions
//   • Global state uses OnceLock<Mutex<>> — safe in multi-threaded JNI

#![cfg(target_os = "android")]
#![allow(non_snake_case)]

mod npu_accelerator;
mod zk_oracle;
mod hybrid_sign;
mod mesh_sync;

use jni::objects::JClass;
use jni::sys::{jboolean, jfloatArray, jint, jstring, JNI_FALSE, JNI_TRUE};
use jni::JNIEnv;
use std::sync::{Mutex, OnceLock};

// ── Thread-safe global NPU engine ──────────────────────────────────────────
// FIX: Previously `static mut Option<NpuEngine>` — data race in JNI threads.
// OnceLock<Mutex<>> provides one-time init + safe interior mutability.
static NPU_ENGINE: OnceLock<Mutex<npu_accelerator::NpuEngine>> = OnceLock::new();

// ── JNI_OnLoad — register logger ───────────────────────────────────────────
#[no_mangle]
pub extern "C" fn JNI_OnLoad(
    _vm: *mut jni::sys::JavaVM,
    _reserved: *mut std::ffi::c_void,
) -> jni::sys::jint {
    android_logger::init_once(
        android_logger::Config::default().with_tag("LennitCore"),
    );
    log::info!("lennit-genesis-core v2.0.0 loaded");
    jni::sys::JNI_VERSION_1_6
}

// ── initializeNpu ──────────────────────────────────────────────────────────
#[no_mangle]
pub extern "system" fn Java_com_lennit_cryptolyzer_AgentBridge_initializeNpu(
    mut env: JNIEnv,
    _class: JClass,
    thermal_limit: jint,
) -> jboolean {
    let result = std::panic::catch_unwind(|| {
        NPU_ENGINE.get_or_init(|| {
            Mutex::new(npu_accelerator::NpuEngine::new(thermal_limit))
        });
    });

    match result {
        Ok(_) => {
            log::info!("NPU engine initialised (thermal_limit={})", thermal_limit);
            JNI_TRUE
        }
        Err(e) => {
            let msg = format!("NPU init panic: {:?}", e);
            let _ = env.throw_new("java/lang/RuntimeException", &msg);
            JNI_FALSE
        }
    }
}

// ── executeInference ───────────────────────────────────────────────────────
#[no_mangle]
pub extern "system" fn Java_com_lennit_cryptolyzer_AgentBridge_executeInference(
    mut env: JNIEnv,
    _class: JClass,
    input_data: jfloatArray,
) -> jfloatArray {
    // Guard: get array length
    let length = match env.get_array_length(&input_data) {
        Ok(l) => l as usize,
        Err(e) => {
            let _ = env.throw_new("java/lang/IllegalArgumentException",
                                  &format!("get_array_length: {e}"));
            return unsafe { jni::sys::JObject::null().into_raw() as jfloatArray };
        }
    };

    // Copy input buffer
    let mut buf = vec![0.0f32; length];
    if let Err(e) = env.get_float_array_region(&input_data, 0, &mut buf) {
        let _ = env.throw_new("java/lang/RuntimeException",
                              &format!("get_float_array_region: {e}"));
        return unsafe { jni::sys::JObject::null().into_raw() as jfloatArray };
    }

    // Run inference through the NPU engine
    let result = NPU_ENGINE
        .get()
        .and_then(|mutex| mutex.lock().ok())
        .map(|engine| engine.execute_inference(buf))
        .unwrap_or_else(|| vec![0.0f32; 1]);

    // Allocate and populate output array
    match env.new_float_array(result.len() as i32) {
        Ok(out) => {
            let _ = env.set_float_array_region(&out, 0, &result);
            out.into_raw()
        }
        Err(e) => {
            let _ = env.throw_new("java/lang/OutOfMemoryError",
                                  &format!("new_float_array: {e}"));
            unsafe { jni::sys::JObject::null().into_raw() as jfloatArray }
        }
    }
}

// ── proveSolvency (ZK Oracle) ──────────────────────────────────────────────
#[no_mangle]
pub extern "system" fn Java_com_lennit_cryptolyzer_AgentBridge_proveSolvency(
    mut env: JNIEnv,
    _class: JClass,
    balance: jni::sys::jlong,
    threshold: jni::sys::jlong,
) -> jstring {
    let proof = zk_oracle::prove_solvency(balance as u64, threshold as u64);
    let hex = proof.iter().map(|b| format!("{:02x}", b)).collect::<String>();
    match env.new_string(hex) {
        Ok(s) => s.into_raw(),
        Err(e) => {
            let _ = env.throw_new("java/lang/RuntimeException",
                                  &format!("new_string: {e}"));
            std::ptr::null_mut()
        }
    }
}
