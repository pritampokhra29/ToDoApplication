// Manual mock for axios to provide CommonJS-compatible functions for tests.
const mockAxios = {
  get: jest.fn(() => Promise.resolve({ data: {} })),
  post: jest.fn(() => Promise.resolve({ data: {} })),
  put: jest.fn(() => Promise.resolve({ data: {} })),
  delete: jest.fn(() => Promise.resolve({ data: {} })),
  create: function () {
    return this;
  },
  // Provide interceptors shape used by the ApiService
  interceptors: {
    request: { use: jest.fn() },
    response: { use: jest.fn() }
  }
};

module.exports = mockAxios;
