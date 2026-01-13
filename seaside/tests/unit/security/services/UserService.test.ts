import { describe, expect, test, vi, afterEach, beforeEach } from 'vitest';
import { firstValueFrom, of } from 'rxjs';
import { send } from '@/common/services/GraphQLClient';
import {
    userCreate,
    userDelete,
    userGet,
    userList,
    userUpdate,
} from '@/security/services/UserService';
import { GraphqlResponse } from '@/common/model/GraphqlResponse.type';
import { ConstantFilters } from '@/constants';

vi.mock('@/common/services/GraphQLClient', () => ({
    send: vi.fn(),
}));

describe('UserService', () => {
    const mockedSend = vi.mocked(send);

    beforeEach(() => {
        vi.clearAllMocks();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    test('should fetch user by id', async () => {
        const user = { _id: 'ULID', login: 'neo', name: 'Neo', mail: 'neo@matrix.io', roles: ['USER'] };
        mockedSend.mockReturnValueOnce(of({
            data: { userGet: user },
        } as GraphqlResponse<unknown>));

        const actual$ = userGet('ULID');

        await expect(firstValueFrom(actual$)).resolves.toEqual(user);
        expect(mockedSend).toHaveBeenCalledWith(expect.stringContaining('query UserGet'), { id: 'ULID' });
    });

    test('should list users and convert dates', async () => {
        const response = {
            totalCount: 40,
            entities: [{
                _id: 'ULID',
                _createdAt: '2024-01-01T00:00:00Z',
                _loginAt: undefined,
                _loginIP: '127.0.0.1',
                login: 'neo',
                name: 'Neo',
                mail: 'neo@matrix.io',
                roles: ['USER'],
            }],
        };
        mockedSend.mockReturnValueOnce(of({
            data: { userSearch: response },
        } as GraphqlResponse<unknown>));

        const localeSpy = vi.spyOn(Date.prototype, 'toLocaleString')
            .mockReturnValueOnce('created-locale')
            .mockReturnValueOnce('epoch-locale');

        const page = await firstValueFrom(userList(2, new URLSearchParams(`?${ConstantFilters.PER_PAGE}=10`)));
        const users = await firstValueFrom(page.data);

        expect(page.currentPage).toBe(2);
        expect(page.totalPage).toBe(2);
        expect(users).toHaveLength(1);
        expect(users[0]._createdAt).toBe('created-locale');
        expect(users[0]._loginAt).toBe('epoch-locale');

        localeSpy.mockRestore();
        expect(mockedSend).toHaveBeenCalledWith(expect.stringContaining('query LoadUsersAdminList'));
    });

    test('should create user', async () => {
        const payload = { login: 'neo', name: 'Neo', mail: 'neo@matrix.io', password: 'secret', roles: ['USER'] };
        mockedSend.mockReturnValueOnce(of({
            data: { userCreate: { ...payload, _id: 'ULID' } },
        } as GraphqlResponse<unknown>));

        const actual$ = userCreate(payload);

        await expect(firstValueFrom(actual$)).resolves.toEqual({ ...payload, _id: 'ULID' });
        expect(mockedSend).toHaveBeenCalledWith(expect.stringContaining('mutation CreateNewUser'), { user: payload });
    });

    test('should delete users', async () => {
        const deleted = [{ _id: 'U1' }, { _id: 'U2' }];
        mockedSend.mockReturnValueOnce(of({
            data: { userDelete: deleted },
        } as GraphqlResponse<unknown>));

        const actual$ = userDelete(['U1', 'U2']);

        await expect(firstValueFrom(actual$)).resolves.toEqual(deleted);
        expect(mockedSend).toHaveBeenCalledWith(expect.stringContaining('mutation DeleteUsers'), { ids: ['U1', 'U2'] });
    });

    test('should update user without meta fields', async () => {
        const responseUser = { _id: 'ULID', login: 'neo', name: 'Neo', mail: 'neo@matrix.io', roles: ['USER'] };
        mockedSend.mockReturnValueOnce(of({
            data: { userUpdate: responseUser },
        } as GraphqlResponse<unknown>));

        const payload = {
            _id: 'ULID',
            _createdAt: 'ignore',
            _loginAt: 'ignore',
            login: 'neo',
            name: 'Neo',
            mail: 'neo@matrix.io',
        };

        const actual$ = userUpdate('ULID', payload, 'current-password');

        await expect(firstValueFrom(actual$)).resolves.toEqual(responseUser);
        expect(mockedSend).toHaveBeenCalledWith(
            expect.stringContaining('mutation UpdateNewUser'),
            {
                id: 'ULID',
                currentPassword: 'current-password',
                user: { login: 'neo', name: 'Neo', mail: 'neo@matrix.io' },
            },
        );
    });
});