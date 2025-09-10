const path = require("path")
const HtmlWebpackPlugin = require("html-webpack-plugin")
const { CleanWebpackPlugin } = require("clean-webpack-plugin")
const ReactRefreshWebpackPlugin = require("@pmmmwh/react-refresh-webpack-plugin")
const ReactRefreshTypeScript = require("react-refresh-typescript").default

// 快速开发配置 - 优化构建速度
const webpackConfig = {
    mode: "development",
    entry: {
        app: "./src/index.tsx",
        // 暂时移除 worker 文件以加快构建
        // "editor.worker": "monaco-editor/esm/vs/editor/editor.worker.js",
        // "json.worker": "monaco-editor/esm/vs/language/json/json.worker",
        // "css.worker": "monaco-editor/esm/vs/language/css/css.worker",
        // "html.worker": "monaco-editor/esm/vs/language/html/html.worker",
        // "ts.worker": "monaco-editor/esm/vs/language/typescript/ts.worker"
    },
    module: {
        rules: [
            {
                test: /froala-editor/,
                parser: {
                    amd: false
                }
            },
            {
                test: /\.tsx?$/,
                use: [
                    {
                        loader: require.resolve("ts-loader"),
                        options: {
                            getCustomTransformers: () => ({
                                before: [ReactRefreshTypeScript()].filter(Boolean)
                            }),
                            transpileOnly: true,
                            happyPackMode: true,
                            experimentalWatchApi: true,
                            configFile: path.resolve(__dirname, "../tsconfig.json")
                        }
                    }
                ],
                exclude: /node_modules/
            },
            {
                test: /\.css$/,
                use: [
                    {
                        loader: "style-loader",
                        options: { injectType: "styleTag" }
                    },
                    "css-loader"
                ]
            },
            {
                test: /\.s[ac]ss$/i,
                use: [
                    {
                        loader: "style-loader",
                        options: { injectType: "styleTag" }
                    },
                    "css-loader",
                    {
                        loader: "sass-loader",
                        options: {
                            api: "modern",
                            sassOptions: {}
                        }
                    }
                ]
            },
            {
                test: /\.(png|jpg|gif|woff|woff2|eot|ttf|otf)$/,
                type: "asset/resource",
                generator: {
                    filename: "assets/[name].[hash:8][ext]"
                }
            },
            {
                test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
                use: [{ loader: "@svgr/webpack", options: { icon: true } }]
            }
        ]
    },
    resolve: {
        extensions: [".tsx", ".ts", ".js", ".html", ".mjs"],
        alias: {
            "@": path.resolve(__dirname, "..", "src")
        }
    },
    devtool: "eval-cheap-module-source-map",
    // 开发环境性能优化
    stats: {
        modules: false,
        children: false,
        chunks: false,
        chunkModules: false,
        assets: false,
        timings: true,
        builtAt: false,
        version: false,
    },
    // 优化开发环境性能
    optimization: {
        removeAvailableModules: false,
        removeEmptyChunks: false,
        splitChunks: false,
        usedExports: false,
        sideEffects: false,
        moduleIds: 'named',
        chunkIds: 'named',
    },
    devServer: {
        hot: true,
        host: "localhost",
        port: 3000,
        historyApiFallback: true,
        open: true,
        compress: true,
        client: {
            overlay: {
                errors: true,
                warnings: false,
            },
        },
        static: {
            directory: path.join(__dirname, "../dist")
        },
        watchFiles: {
            paths: ['src/**/*'],
            options: {
                usePolling: false,
                ignored: /node_modules/,
            },
        },
        proxy: {
            "/gw": {
                target: "http://117.50.245.4:8090",
                changeOrigin: true,
                ws: true,
                logLevel: "debug",
                pathRewrite: {
                    "^/gw": ""
                }
            }
        }
    },
    cache: {
        type: "filesystem",
        buildDependencies: {
            config: [__filename]
        },
        cacheDirectory: path.resolve(__dirname, "../node_modules/.cache/webpack"),
        compression: "gzip",
        maxAge: 1000 * 60 * 60 * 24 * 7, // 7天
    },
    plugins: [
        new CleanWebpackPlugin(),
        new ReactRefreshWebpackPlugin({
            overlay: false
        }),
        // 移除 ForkTsCheckerWebpackPlugin 以加快构建
        new HtmlWebpackPlugin({
            template: "./src/index.html",
            chunks: ["app"]
        })
    ],
    output: {
        filename: "[name].bundle.js",
        path: path.resolve(__dirname, "../dist"),
        publicPath: "/"
    }
}

module.exports = webpackConfig
