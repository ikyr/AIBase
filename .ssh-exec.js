const { Client } = require('ssh2');
const path = require('path');
const fs = require('fs');

const conn = new Client();
const [host, cmd] = process.argv.slice(2);

if (!host || !cmd) {
  console.log('Usage: node .ssh-exec.js <host> <command>');
  process.exit(1);
}

conn.on('ready', () => {
  conn.exec(cmd, (err, stream) => {
    if (err) throw err;
    let out = '', errOut = '';
    stream.on('data', (d) => { out += d.toString(); });
    stream.stderr.on('data', (d) => { errOut += d.toString(); });
    stream.on('close', (code) => {
      if (out) process.stdout.write(out);
      if (errOut) process.stderr.write(errOut);
      conn.end();
      process.exit(code);
    });
  });
}).on('error', (e) => {
  process.stderr.write('SSH error: ' + e.message + '\n');
  process.exit(1);
});

const username = process.env.DEPLOY_USER || 'root';
const password = process.env.DEPLOY_PASSWORD;

if (!password) {
  console.error('DEPLOY_PASSWORD env var is required');
  process.exit(1);
}

conn.connect({
  host,
  port: 22,
  username,
  password,
  readyTimeout: 10000,
});
