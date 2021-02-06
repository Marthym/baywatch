module.exports = {
    devServer: {
        headers: {
            "Access-Control-Allow-Origin": "*",
            https: true
        },
        proxy: {
            '^/api': {
                target: 'http://localhost:8081',
                toProxy: true,
            },
        },
    }
}