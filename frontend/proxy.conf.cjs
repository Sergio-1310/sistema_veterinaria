module.exports = {
  '/api/**': { target: process.env.HV_API_TARGET || 'http://127.0.0.1:8080', secure: false }
};
