const Tesseract = require('tesseract.js');

const runOCR = async () => {
  try {
    const { data: { text } } = await Tesseract.recognize(
      'public/report.png',
      'eng', // language
      { logger: info => console.log(info) } // progress logs
    );
    console.log('Extracted Text:');
    console.log(text);
  } catch (err) {
    console.error('OCR Error:', err);
  }
};

runOCR();
