// MODULE 11: Quantum-Safe Signing (NIST FIPS 204)
pub fn sign_transaction(data: &[u8], key: &SecretKey) -> Vec<u8> {
    ml_dsa_65::sign(data, key)
}