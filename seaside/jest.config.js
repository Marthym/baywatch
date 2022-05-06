const config = {
    verbose: true,
};

module.exports = config;

// Or async function
module.exports = async () => {
    return {
        verbose: true,
        preset: 'ts-jest',
        transform: {
            // ...
            // process `*.ts` files with `ts-jest`
            "^.+\\.tsx?$": "ts-jest"
        },
        moduleNameMapper: {
            '^@/(.*)$': '<rootDir>/src/$1',
        },
        testEnvironment: 'jsdom',
    };
};
