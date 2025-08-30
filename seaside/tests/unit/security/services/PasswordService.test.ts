import { describe, expect, test, vi } from 'vitest';
import {
    passwordAnonymousCheckStrength,
    passwordAskForReset,
    passwordCheckStrength,
    passwordGenerate,
    passwordReset,
} from '@/security/services/PasswordService';
import { firstValueFrom, of } from 'rxjs';
import { send } from '@/common/services/GraphQLClient';
import { GraphqlResponse } from '@/common/model/GraphqlResponse.type';

vi.mock('@/common/services/GraphQLClient', () => {
    return {
        send: vi.fn(),
    };
});

describe('PasswordService', () => {
    test('should test strength for anonymous', async () => {
        vi.mocked(send).mockReturnValueOnce(of({
            data: {
                passwordCheckAnonymous: {
                    strength: 'weak', message: 'Password is too weak',
                },
            },
        } as GraphqlResponse<unknown>));

        const actual = passwordAnonymousCheckStrength({
            login: 'okenobi',
            name: 'Obiwan Kenobi',
            mail: 'okenobi@jedi.com',
            password: 'newPassword',
            roles: ['USER'],
        });

        await expect(firstValueFrom(actual)).resolves.toEqual({
            strength: 'weak', message: 'Password is too weak',
        });
    });

    test('should test password strength', async () => {
        vi.mocked(send).mockReturnValueOnce(of({
            data: {
                passwordCheckStrength: {
                    strength: 'weak', message: 'Password is too weak',
                },
            },
        } as GraphqlResponse<unknown>));

        const actual = passwordCheckStrength('newPassword');

        await expect(firstValueFrom(actual)).resolves.toEqual({
            strength: 'weak', message: 'Password is too weak',
        });
    });

    test('should generate password', async () => {
        vi.mocked(send).mockReturnValueOnce(of({
            data: {
                passwordGenerate: ['pass1', 'pass2'],
            },
        } as GraphqlResponse<unknown>));

        const actual = passwordGenerate(2);

        await expect(firstValueFrom(actual)).resolves.toEqual(['pass1', 'pass2']);
    });

    test('should ask for password reset', async () => {
        vi.mocked(send).mockReturnValueOnce(of({
            data: {},
        } as GraphqlResponse<unknown>));

        const actual = passwordAskForReset('okenobi');

        await expect(firstValueFrom(actual)).resolves.toBeUndefined();
    });

    test('should fail ask for password reset', async () => {
        vi.mocked(send).mockReturnValueOnce(of({
            data: {},
            errors: [{
                message: 'User not found',
                extensions: { classification: 'INTERNAL_ERROR' },
            }],
        } as GraphqlResponse<unknown>));

        const actual = passwordAskForReset('okenobi');

        await expect(firstValueFrom(actual)).rejects.toEqual({
            message: 'User not found',
            extensions: { classification: 'INTERNAL_ERROR' },
        });
    });

    test('should reset password', async () => {
        vi.mocked(send).mockReturnValueOnce(of({
            data: {
                passwordReset: {},
            },
        } as GraphqlResponse<unknown>));

        const actual = passwordReset('token', 'newPassword');

        await expect(firstValueFrom(actual)).resolves.toBeUndefined();
    });

    test('should fail reset password', async () => {
        vi.mocked(send).mockReturnValueOnce(of({
            data: {},
            errors: [{
                message: 'User not found',
                extensions: { classification: 'INTERNAL_ERROR' },
            }],
        } as GraphqlResponse<unknown>));

        const actual = passwordReset('token', 'newPassword');

        await expect(firstValueFrom(actual)).rejects.toEqual({
            message: 'User not found',
            extensions: { classification: 'INTERNAL_ERROR' },
        });
    });
});