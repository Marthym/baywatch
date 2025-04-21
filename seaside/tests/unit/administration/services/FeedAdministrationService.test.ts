import { describe, expect, test, vi } from 'vitest';
import { lastValueFrom, of } from 'rxjs';

vi.mock('@/common/services/GraphQLClient', () => {
    return {
        send: vi.fn().mockReturnValue(of({
            data: {
                adminRawFeedFind: {
                    totalCount: 40,
                    entities: [{
                        _id: '42',
                        name: 'Jedi',
                        description: 'The jedi channel',
                        url: 'https://jedi.com/atom.xml',
                        icon: 'https://jedi.com/favicon.ico',
                        lastWatch: new Date('2022-01-01T00:00:00.000Z'),
                        lastETag: '1234567890',
                        error: {},
                    }, {
                        _id: '66',
                        name: 'Sith',
                        description: 'The Sith channel',
                        url: 'https://sith.com/atom.xml',
                        icon: 'https://sith.com/favicon.ico',
                        lastWatch: new Date('2022-01-01T00:00:00.000Z'),
                        lastETag: '1234567890',
                        error: {},
                    }],
                },
                adminRawFeedDelete: {},
            },
        })),
    };
});

const { send } = await import('@/common/services/GraphQLClient');
const { adminFeedFind, adminRawFeedDelete } = (await import('@/administration/services/FeedAdministrationService'));

describe('FeedAdministrationService', () => {
    test('should find raw feeds', async () => {
        const actuals = await lastValueFrom(adminFeedFind({ _p: 1, _pp: 20 }));

        expect(send).toHaveBeenCalled();
        expect(actuals.totalPage).toEqual(2);
        expect(await lastValueFrom(actuals.data)).toHaveLength(2);
    });

    test('should delete feed', async () => {
        const actual = await lastValueFrom(adminRawFeedDelete(['42']));

        expect(send).toHaveBeenCalled();
        expect(actual).toBeUndefined();
    });

});