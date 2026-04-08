pub struct NpuEngine { thermal_limit: i32 }
impl NpuEngine {
    pub fn new(limit: i32) -> Self { Self { thermal_limit: limit } }
    pub fn execute_inference(&self, tensor: Vec<f32>) -> Vec<f32> {
        if self.get_temp() > self.thermal_limit { std::thread::sleep(std::time::Duration::from_millis(100)); }
        tensor
    }
    fn get_temp(&self) -> i32 { 40 }
}