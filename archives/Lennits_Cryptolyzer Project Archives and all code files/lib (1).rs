#![cfg(target_os = "android")]
#![allow(non_snake_case)]

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jboolean, jfloatArray, jlong, jbyteArray, jint};

mod npu_accelerator;
mod zk_oracle;

// Global instance of the NPU Engine
static mut NPU_ENGINE: Option<npu_accelerator::NpuEngine> = None;

#[no_mangle]
pub extern "system" fn Java_com_lennit_core_AgentBridge_initializeNpu(
    _env: JNIEnv,
    _class: JClass,
    thermal_limit: jint,
) -> jboolean {
    println!("> LENNIT_GENESIS: BINDING SNAPDRAGON 865 NPU...");
    unsafe {
        NPU_ENGINE = Some(npu_accelerator::NpuEngine::new(thermal_limit));
    }
    1 // Return true
}

#[no_mangle]
pub extern "system" fn Java_com_lennit_core_AgentBridge_executeInference(
    env: JNIEnv,
    _class: JClass,
    input_data: jfloatArray,
) -> jfloatArray {
    // 1. Convert Java FloatArray to Rust Vec<f32>
    let length = env.get_array_length(input_data).unwrap();
    let mut buffer = vec![0.0f32; length as usize];
    env.get_float_array_region(input_data, 0, &mut buffer).unwrap();

    // 2. Execute on NPU
    let result = unsafe {
        if let Some(engine) = &NPU_ENGINE {
            engine.execute_inference(buffer)
        } else {
            vec![0.0f32] // Fallback if uninitialized
        }
    };

    // 3. Convert back to Java FloatArray
    let output_array = env.new_float_array(result.len() as i32).unwrap();
    env.set_float_array_region(output_array, 0, &result).unwrap();
    output_array
}
