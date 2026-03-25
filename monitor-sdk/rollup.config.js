import resolve from '@rollup/plugin-node-resolve'
import commonjs from '@rollup/plugin-commonjs'
import babel from '@rollup/plugin-babel'
import terser from '@rollup/plugin-terser'
import json from '@rollup/plugin-json'

export default {
  input: 'src/index.js',
  
  output: [
    {
      file: 'dist/monitor-sdk.js',
      format: 'umd',
      name: 'MonitorSDK',
      sourcemap: true,
      globals: {
        'pako': 'pako'
      }
    },
    {
      file: 'dist/monitor-sdk.min.js',
      format: 'umd',
      name: 'MonitorSDK',
      sourcemap: true,
      plugins: [terser()],
      globals: {
        'pako': 'pako'
      }
    },
    {
      file: 'dist/monitor-sdk.esm.js',
      format: 'es',
      sourcemap: true
    },
    {
      file: 'dist/monitor-sdk.cjs.js',
      format: 'cjs',
      sourcemap: true
    }
  ],
  
  plugins: [
    resolve({
      browser: true,
      preferBuiltins: false
    }),
    commonjs(),
    json(),
    babel({
      babelHelpers: 'bundled',
      exclude: 'node_modules/**',
      presets: [
        ['@babel/preset-env', {
          targets: {
            browsers: ['> 1%', 'last 2 versions', 'not dead']
          },
          modules: false
        }]
      ]
    })
  ],
  
  external: ['pako']
}
