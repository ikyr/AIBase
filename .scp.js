const { Client } = require('ssh2');
const fs = require('fs');

const conn = new Client();
const localFileArg = process.argv[2];
const remotePathArg = process.argv[3];

if (!localFileArg || !remotePathArg) {
  console.log('Usage: node .scp.js <localFile> <//remote/path>');
  console.log('Note: remote path MUST start with // to bypass Git Bash path conversion');
  process.exit(1);
}

// Strip one leading slash (added to bypass Git Bash)
const remotePath = String(remotePathArg).startsWith('//')
  ? String(remotePathArg).substring(1)
  : String(remotePathArg);

// Find actual local file
let resolvedPath = String(localFileArg).replace(/\\/g, '/');
if (!fs.existsSync(resolvedPath)) {
  resolvedPath = String(process.env.TEMP || '').replace(/\\/g, '/') + '/'
    + String(localFileArg).replace(/\\/g, '/').replace(/^.*[/]/, '');
}
if (!fs.existsSync(resolvedPath)) {
  console.error('Local file not found:', localFileArg, resolvedPath);
  process.exit(1);
}

const fileSize = fs.statSync(resolvedPath).size;
const remoteDir = remotePath.replace(/\\/g, '/').replace(/\/[^/]+$/, '');
console.log('Upload:', fileSize, 'bytes ->', remotePath);

conn.on('ready', () => {
  conn.sftp((err, sftp) => {
    if (err) { console.error('SFTP error:', err.message); process.exit(1); }

    const parts = remoteDir.split('/').filter(Boolean);
    const createNext = (idx) => {
      if (idx > parts.length) {
        sftp.fastPut(resolvedPath, remotePath, (err) => {
          if (err) { console.error('Put error:', err.message); process.exit(1); }
          console.log('OK: ' + fileSize + ' bytes -> ' + remotePath);
          conn.end();
          process.exit(0);
        });
        return;
      }
      const dir = '/' + parts.slice(0, idx).join('/');
      sftp.mkdir(dir, () => createNext(idx + 1));
    };
    createNext(1);
  });
}).on('error', (e) => {
  console.error('SSH error:', e.message);
  process.exit(1);
});

conn.connect({
  host: '10.139.11.100',
  port: 22,
  username: 'root',
  password: 'Dt1q2w3e4r!',
  readyTimeout: 15000,
});
