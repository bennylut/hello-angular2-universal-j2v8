var webpack = require('webpack');
var path = require('path');
var CopyWebpackPlugin = require('copy-webpack-plugin');

var commonConfig = {
  resolve: {
    extensions: ['', '.ts', '.js']
  },
  module: {
    loaders: [
      // TypeScript
      { test: /\.ts$/, loader: 'ts-loader' }
    ]
  },
  plugins: [
    new webpack.optimize.OccurenceOrderPlugin(true)
  ]
};


var clientConfig = {
  target: 'web',
  entry: './src/main/angular2/client',
  output: {
    path: path.join(__dirname,'ng2-target', 'client')
  },
  plugins: [
    new CopyWebpackPlugin([{
      from: __dirname + '/src/main/resources/client',
      to: __dirname + '/assets'
    }])
  ],
  node: {
    global: true,
    __dirname: true,
    __filename: true,
    process: true,
    Buffer: false
  }
};


var serverConfig = {
  target: 'node',
  entry: './src/main/angular2/server',
  output: {
    path: path.join(__dirname,'ng2-target', 'server')
  },
  externals: checkNodeImport,
  node: {
    global: true,
    __dirname: true,
    __filename: true,
    process: true,
    Buffer: true
  }
};



// Default config
var defaultConfig = {
  module: {
    noParse: [
      path.join(__dirname, 'zone.js', 'dist'),
      path.join(__dirname, 'angular2', 'bundles')
    ]
  },
  context: __dirname,
  resolve: {
    root: path.join(__dirname, '/src/main/angular2')
  },
  output: {
    publicPath: path.resolve(__dirname),
    filename: 'bundle.js'
  }
}



var webpackMerge = require('webpack-merge');
module.exports = [
  // Client
  webpackMerge({}, defaultConfig, commonConfig, clientConfig),

  // Server
  webpackMerge({}, defaultConfig, commonConfig, serverConfig)
]

// Helpers
function checkNodeImport(context, request, cb) {
  if (!path.isAbsolute(request) && request.charAt(0) !== '.') {
    cb(null, 'commonjs ' + request); return;
  }
  cb();
}
