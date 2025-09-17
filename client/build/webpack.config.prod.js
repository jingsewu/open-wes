const path = require("path")
const HtmlWebpackPlugin = require("html-webpack-plugin")
const { CleanWebpackPlugin } = require("clean-webpack-plugin")
const TerserPlugin = require("terser-webpack-plugin")
const MiniCssExtractPlugin = require("mini-css-extract-plugin")
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin")

module.exports = {
    mode: "production",
    context: path.resolve(__dirname, "../"),
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
                        loader: "ts-loader",
                        options: {
                            transpileOnly: true,
                            happyPackMode: true
                        }
                    }
                ],
                exclude: /node_modules/
            },
            {
                test: /\.css$/,
                use: [
                    MiniCssExtractPlugin.loader,
                    {
                        loader: "css-loader",
                        options: {
                            importLoaders: 1
                        }
                    }
                ]
            },
            {
                test: /\.s[ac]ss$/i,
                use: [
                    MiniCssExtractPlugin.loader,
                    {
                        loader: "css-loader",
                        options: {
                            importLoaders: 2
                        }
                    },
                    {
                        loader: "sass-loader",
                        options: {
                            api: "modern",
                            sassOptions: {
                                outputStyle: "compressed"
                            }
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
    optimization: {
        minimize: true,
        minimizer: [
            new TerserPlugin({
                parallel: true,
                terserOptions: {
                    compress: {
                        drop_console: true,
                        drop_debugger: true
                    }
                }
            })
            // 暂时禁用CSS压缩，避免与某些CSS文件冲突
            // new CssMinimizerPlugin()
        ],
        splitChunks: {
            chunks: "all",
            cacheGroups: {
                vendor: {
                    test: /[\\/]node_modules[\\/]/,
                    name: "vendors",
                    chunks: "all",
                    priority: 10
                },
                monaco: {
                    test: /[\\/]node_modules[\\/]monaco-editor[\\/]/,
                    name: "monaco",
                    chunks: "all",
                    priority: 20
                },
                common: {
                    name: "common",
                    minChunks: 2,
                    chunks: "all",
                    priority: 5,
                    reuseExistingChunk: true
                }
            }
        }
    },
    cache: {
        type: "filesystem",
        buildDependencies: {
            config: [__filename]
        }
    },
    plugins: [
        new CleanWebpackPlugin(),
        new MiniCssExtractPlugin({
            filename: "css/[name].[contenthash:8].css",
            chunkFilename: "css/[name].[contenthash:8].chunk.css"
        }),
        new HtmlWebpackPlugin({
            template: "./src/index.html",
            chunks: ["app", "vendors", "common"],
            minify: {
                removeComments: true,
                collapseWhitespace: true,
                removeRedundantAttributes: true,
                useShortDoctype: true,
                removeEmptyAttributes: true,
                removeStyleLinkTypeAttributes: true,
                keepClosingSlash: true,
                minifyJS: true,
                minifyCSS: true,
                minifyURLs: true
            }
        })
    ],
    output: {
        filename: "js/[name].[contenthash:8].js",
        chunkFilename: "js/[name].[contenthash:8].chunk.js",
        path: path.resolve(__dirname, "../dist"),
        publicPath: "/",
        clean: true
    }
}
