import { describe, expect, test, vi } from 'vitest';
import { lastValueFrom, of } from 'rxjs';

vi.doMock('@/common/services/GraphQLClient', () => {
    return {
        send: vi.fn().mockReturnValue(of({
            data: {
                feedsSearch: {
                    totalCount: 40,
                    entities: [{
                        _id: '42',
                        name: 'Jedi',
                        description: 'The jedi channel',
                        location: 'https://jedi.com/atom.xml',
                        tags: ['light', 'good'],
                        error: {},
                    }, {
                        _id: '66',
                        name: 'Sith',
                        description: 'The Sith channel',
                        location: 'https://sith.com/atom.xml',
                        tags: ['dark', 'bad'],
                        error: {},
                    }],
                },
                scrapFeedHeader: {
                    title: 'Test tile',
                    description: 'Testdescription',
                },
                feedUpdate: {
                    _id: '66',
                    name: 'Sith',
                    description: 'The Sith channel',
                    location: 'https://sith.com/atom.xml',
                    tags: ['dark', 'bad'],
                    error: {},
                },
                feedAddAndSubscribe: {
                    _id: '42',
                    name: 'Jedi',
                    description: 'The jedi channel',
                    location: 'https://jedi.com/atom.xml',
                    tags: ['light', 'good'],
                    error: {},
                },
                feedDelete: {
                    _id: '42',
                    name: 'Jedi',
                },
                subscribe: {
                    _id: '42',
                    name: 'Jedi',
                    description: 'The jedi channel',
                    location: 'https://jedi.com/atom.xml',
                    tags: ['light', 'good'],
                    error: {},
                },
            },
        })),
    };
});

const { send } = await import('@/common/services/GraphQLClient');
const feedService = (await import('@/configuration/services/FeedService')).default;
const { feedUpdate, feedAddAndSubscribe, feedDelete } = (await import('@/configuration/services/FeedService'));

describe('Configuration FeedService', () => {
    test('should list feeds', async () => {
        const actuals = await lastValueFrom(feedService.list({ _p: 1, _pp: 20 }));

        expect(send).toHaveBeenCalled();
        expect(actuals.totalPage).toEqual(2);
        expect(await lastValueFrom(actuals.data)).toHaveLength(2);
    });

    test('should fetch feed information', async () => {
        const actual = await lastValueFrom(feedService.fetchFeedInformation('https://jedi.com/atom.xml'));

        expect(send).toHaveBeenCalled();
        expect(actual).toEqual({ description: 'Testdescription', name: 'Test tile' });
    });

    test('should update feed', async () => {
        const actual = await lastValueFrom(feedUpdate('42', {
            name: 'New Name',
            description: 'New description',
            tags: ['new', 'tags'],
        }));

        expect(send).toHaveBeenCalled();
        expect(actual).toEqual({
            _id: '66',
            name: 'Sith',
            description: 'The Sith channel',
            location: 'https://sith.com/atom.xml',
            tags: ['dark', 'bad'],
            error: {},
        });
    });

    test('should add and subscribe feed', async () => {
        const actual = await lastValueFrom(feedAddAndSubscribe({
            name: 'New Name',
            description: 'New description',
            tags: ['new', 'tags'],
            location: 'https://jedi.com/atom.xml',
        }));

        expect(send).toHaveBeenCalled();
        expect(actual).toEqual({
            _id: '42',
            name: 'Jedi',
            description: 'The jedi channel',
            location: 'https://jedi.com/atom.xml',
            tags: ['light', 'good'],
            error: {},
        });
    });

    test('should delete feed', async () => {
        const actual = await lastValueFrom(feedDelete(['42']));

        expect(send).toHaveBeenCalled();
        expect(actual).toEqual({
            _id: '42',
            name: 'Jedi',
        });
    });

    test('should subscribe only', async () => {
        const actual = await lastValueFrom(feedService.subscribe('42'));

        expect(send).toHaveBeenCalled();
        expect(actual).toEqual({
            _id: '42',
            name: 'Jedi',
            description: 'The jedi channel',
            location: 'https://jedi.com/atom.xml',
            tags: ['light', 'good'],
            error: {},
        });
    });
});