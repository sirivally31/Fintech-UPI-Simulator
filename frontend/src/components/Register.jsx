import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

function Register() {
  const [formData, setFormData] = useState({
    name: '',
    phoneNumber: '',
    upiId: '',
    pin: '',
    initialBalance: ''
  });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');

    try {
      const payload = {
        ...formData,
        initialBalance: formData.initialBalance ? parseFloat(formData.initialBalance) : 0
      };
      
      await axios.post('/api/users/register', payload);
      alert('Registration successful! Please log in.');
      navigate('/login');
    } catch (err) {
      if (err.response && err.response.data) {
        // If the backend returns a specific error message or validation errors
        if (typeof err.response.data === 'string') {
           setError(err.response.data);
        } else if (err.response.data.message) {
           setError(err.response.data.message);
        } else {
           setError('Failed to register. Please check your inputs.');
        }
      } else {
        setError('An error occurred during registration.');
      }
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-header">
        <h1>Create Account</h1>
        <p>Join the UPI Simulator platform</p>
      </div>

      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleRegister}>
        <div className="form-group">
          <label>Full Name</label>
          <input
            type="text"
            name="name"
            className="form-input"
            value={formData.name}
            onChange={handleChange}
            placeholder="John Doe"
            required
          />
        </div>

        <div className="form-group">
          <label>Phone Number (10 digits)</label>
          <input
            type="tel"
            name="phoneNumber"
            className="form-input"
            value={formData.phoneNumber}
            onChange={handleChange}
            placeholder="9876543210"
            pattern="\d{10}"
            required
          />
        </div>

        <div className="form-group">
          <label>UPI ID</label>
          <input
            type="text"
            name="upiId"
            className="form-input"
            value={formData.upiId}
            onChange={handleChange}
            placeholder="johndoe@upi"
            required
          />
        </div>

        <div className="form-group">
          <label>PIN (4-6 digits)</label>
          <input
            type="password"
            name="pin"
            className="form-input"
            value={formData.pin}
            onChange={handleChange}
            placeholder="••••••"
            minLength={4}
            maxLength={6}
            required
          />
        </div>

        <div className="form-group">
          <label>Initial Balance (Optional)</label>
          <input
            type="number"
            name="initialBalance"
            className="form-input"
            value={formData.initialBalance}
            onChange={handleChange}
            placeholder="1000"
            min={0}
            step="0.01"
          />
        </div>

        <button type="submit" className="btn-primary">
          Sign Up
        </button>
      </form>

      <div className="auth-footer">
        Already have an account? <Link to="/login">Log in</Link>
      </div>
    </div>
  );
}

export default Register;
