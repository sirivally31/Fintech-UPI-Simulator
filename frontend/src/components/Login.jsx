import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

function Login() {
  const [upiId, setUpiId] = useState('');
  const [pin, setPin] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const response = await axios.post('/api/auth/login', { upiId, pin });
      const { token } = response.data;
      localStorage.setItem('token', token);
      // Navigate to dashboard once implemented
      // navigate('/dashboard');
      alert('Login Successful!');
    } catch (err) {
      setError('Invalid UPI ID or PIN');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-header">
        <h1>Welcome Back</h1>
        <p>Log in to your UPI Simulator account</p>
      </div>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleLogin}>
        <div className="form-group">
          <label>UPI ID</label>
          <input
            type="text"
            className="form-input"
            value={upiId}
            onChange={(e) => setUpiId(e.target.value)}
            placeholder="example@upi"
            required
          />
        </div>

        <div className="form-group">
          <label>PIN (4-6 digits)</label>
          <input
            type="password"
            className="form-input"
            value={pin}
            onChange={(e) => setPin(e.target.value)}
            placeholder="••••••"
            maxLength={6}
            required
          />
        </div>

        <button type="submit" className="btn-primary">
          Log In
        </button>
      </form>

      <div className="auth-footer">
        Don't have an account? <Link to="/register">Sign up</Link>
      </div>
    </div>
  );
}

export default Login;
