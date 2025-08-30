import React from 'react';
// Mock axios to prevent Jest from attempting to load ESM entry from node_modules
jest.mock('axios');
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders login header', () => {
  render(<App />);
  const header = screen.getByText(/login to todolist/i);
  expect(header).toBeInTheDocument();
});
