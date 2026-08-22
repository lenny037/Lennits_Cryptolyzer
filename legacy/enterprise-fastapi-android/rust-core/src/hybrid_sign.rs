// MODULE 19: Quantum-resistant hybrid signing (ECDSA + ML-DSA placeholder)
//
// NIST FIPS 204 (ML-DSA / Dilithium) provides post-quantum security.
// This stub will be replaced with the `ml-dsa` crate when stable.

/// Represents a hybrid signature combining classical + post-quantum.
pub struct HybridSignature {
    pub classical_sig:  Vec<u8>,  // ECDSA secp256k1 (32+32 bytes)
    pub pq_sig:         Vec<u8>,  // ML-DSA stub (128 bytes placeholder)
    pub scheme:         HybridScheme,
}

#[derive(Debug, Clone)]
pub enum HybridScheme {
    EcdsaOnly,
    EcdsaMlDsa,   // Future: NIST FIPS 204
}

pub fn sign_message(message: &[u8], _private_key: &[u8]) -> HybridSignature {
    // TODO: implement with secp256k1 crate + ml-dsa crate
    log::debug!("hybrid_sign: signing {} bytes (stub)", message.len());
    HybridSignature {
        classical_sig: vec![0u8; 64],
        pq_sig:        vec![0u8; 128],
        scheme:        HybridScheme::EcdsaOnly,
    }
}

pub fn verify_signature(
    message:   &[u8],
    signature: &HybridSignature,
    _pub_key:  &[u8],
) -> bool {
    // TODO: implement real verification
    log::debug!(
        "hybrid_sign: verify scheme={:?} msg_len={} (stub)",
        signature.scheme, message.len()
    );
    !signature.classical_sig.is_empty()
}
