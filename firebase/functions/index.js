// firebase/functions/index.js

const { verifyTransfer, exportAnalyticsToBigQuery } = require('./verifyTransfer');
const { deleteUserData, exportUserData } = require('./deleteUserData');

// Export all functions
exports.verifyTransfer = verifyTransfer;
exports.exportAnalyticsToBigQuery = exportAnalyticsToBigQuery;
exports.deleteUserData = deleteUserData;
exports.exportUserData = exportUserData;