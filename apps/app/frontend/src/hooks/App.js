import { useEffect, useCallback } from "react";
import "@/App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import axios from "axios";

const BACKEND_URL = process.env.REACT_APP_BACKEND_URL;

const Home = () => {
  const helloWorldApi = useCallback(async () => {
    const API = `${BACKEND_URL}/api`;
    try {
      const response = await axios.get(`${API}/`);
      // API call successful - data available in response.data.message
      // For production, use a proper logging service like Sentry or LogRocket
    } catch (error) {
      // Handle error appropriately
      // Consider using error boundary or error monitoring service
      if (process.env.NODE_ENV === 'development') {
        console.error('API request failed:', error);
      }
    }
  }, [BACKEND_URL]);

  useEffect(() => {
    helloWorldApi();
  }, [helloWorldApi]);

  return (
    <div>
      <header className="App-header">
        <div className="App-link">
          <h1>Full-Stack Application</h1>
          <p className="mt-5">FastAPI + React + MongoDB</p>
        </div>
      </header>
    </div>
  );
};

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home />}>
            <Route index element={<Home />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
