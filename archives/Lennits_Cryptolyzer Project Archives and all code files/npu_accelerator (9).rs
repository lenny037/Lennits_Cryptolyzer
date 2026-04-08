pub struct NpuEngine { thermal_limit: i32 }
impl NpuEngine {
    pub fn execute_inference(&self, data: Vec<f32>) -> Vec<f32> {
        if self.get_temp() > 41 { std::thread::sleep(std::time::Duration::from_millis(100)); }
        data
    }
    fn get_temp(&self) -> i32 { 40 }
}