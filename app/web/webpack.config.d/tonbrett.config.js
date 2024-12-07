const HtmlWebpackPlugin = require("html-webpack-plugin");

config.output.filename = '[name].[contenthash].js';
config.output.library = undefined;
config.plugins.push(new HtmlWebpackPlugin({
    filename: 'index.html',
    template: '../../../../app/web/src/templates/index.template.html',
    publicPath: '/soundboard/ui'
}));
config.plugins.push(new HtmlWebpackPlugin({
    filename: 'discord-activity.html',
    publicPath: "/.proxy/static/",
    template: '../../../../app/web/src/templates/discord-activity.template.html'
}));
config.resolve.fallback = { "os": require.resolve("os-browserify/browser"), "path": require.resolve("path-browserify") }
config.stats = "errors-only"