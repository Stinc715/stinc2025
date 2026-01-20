const express = require('express');
const cors = require('cors');
const app = express();
const port = 8080;

app.use(cors());
app.use(express.json());

app.get('/', (req, res) => res.send('stinc2025 mock backend is running'));

// POST /api/auth/google
app.post('/api/auth/google', (req, res) => {
  try {
    const { credential } = req.body || {};
    if (!credential) return res.status(400).json({ error: 'missing credential' });

    // Try to decode JWT payload (not verification) to extract email/name for mock response
    let payload = null;
    try {
      const parts = credential.split('.');
      if (parts.length >= 2) {
        const b = Buffer.from(parts[1].replace(/-/g,'+').replace(/_/g,'/'), 'base64');
        payload = JSON.parse(b.toString('utf8'));
      }
    } catch (e) {
      payload = null;
    }

    const email = (payload && (payload.email || payload.sub)) || 'user@example.com';
    const fullName = (payload && (payload.name)) || (email.split('@')[0]);

    // Return a mock user object similar to backend expectation
    const user = {
      id: payload && payload.sub ? payload.sub : 'local-mock-' + Math.floor(Math.random()*100000),
      fullName,
      email,
      role: 'USER'
    };

    console.log('[mock-backend] /api/auth/google ->', user.email);
    return res.json(user);
  } catch (err) {
    console.error('[mock-backend] error', err);
    return res.status(500).json({ error: 'internal error' });
  }
});

app.listen(port, () => console.log(`Mock backend listening on http://localhost:${port}`));
