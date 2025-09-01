const path = require("path")
const HtmlWebpackPlugin = require("html-webpack-plugin")
const { CleanWebpackPlugin } = require("clean-webpack-plugin")
const ReactRefreshWebpackPlugin = require("@pmmmwh/react-refresh-webpack-plugin")
const ReactRefreshTypeScript = require("react-refresh-typescript").default

webpackConfig = {
    mode: "development",
    entry: {
        app: "./src/index.tsx",
        "editor.worker": "monaco-editor/esm/vs/editor/editor.worker.js",
        "json.worker": "monaco-editor/esm/vs/language/json/json.worker",
        "css.worker": "monaco-editor/esm/vs/language/css/css.worker",
        "html.worker": "monaco-editor/esm/vs/language/html/html.worker",
        "ts.worker": "monaco-editor/esm/vs/language/typescript/ts.worker"
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
                                before: [ReactRefreshTypeScript()].filter(
                                    Boolean
                                )
                            }),
                            transpileOnly: true
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
                    // "sass-loader"
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
                use: ["file-loader"]
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
    devtool: "inline-source-map",
    devServer: {
        hot: true,
        host: "localhost",
        port: 4000,
        historyApiFallback: true,
        open: true,
        // compress: false,
        proxy: {
            "/gw": {
                target: "http://localhost:8090",
                changeOrigin: true,
                ws: true,
                logLevel: "debug",
                pathRewrite: {
                    "^/gw": ""
                }
            }
        }
    },
    plugins: [
        new CleanWebpackPlugin(),
        new ReactRefreshWebpackPlugin(),
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
