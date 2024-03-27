const path = require('path')

module.exports = {
    mode: 'development',
    resolve: {
        extensions: ['.tsx', '.ts', '.js'],
      },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/,
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader'],
            },
            {
                test: /\.(png|jpe?g|gif)$/i,
                use: [
                    {
                        loader: 'file-loader',
                    },
                ],
            },
        ],
    }, 

    devServer: {
        historyApiFallback: true,
        proxy: {
            '/api-planit': {
                target: 'http://localhost:9000',
                router: () => 'http://localhost:1904',
            }
        },
        port: 9000,
        static: path.resolve(__dirname, 'dist')
    },
};
