const fs = require('fs-extra');
const path = require('path');

const srcDir = path.join(__dirname, 'node_modules', 'govuk-frontend', 'dist', 'govuk');
const destDir = path.join(__dirname, 'src', 'main', 'resources', 'static', 'govuk');

fs.copy(srcDir, destDir, err => {
  if (err) return console.error(err);
  console.log('GOV.UK Frontend assets copied successfully!');
});
