import { describe, expect, test, vi } from 'vitest';
import { lastValueFrom, of, throwError } from 'rxjs';
import {
    adminFeedFind,
    adminFeedUpdate,
    adminRawFeedDelete,
    adminRawFeedGet, adminRawFeedScrap,
} from '@/administration/services/FeedAdministrationService';
import { send } from '@/common/services/GraphQLClient';
import { GraphqlResponse } from '@/common/model/GraphqlResponse.type';

vi.mock('@/common/services/GraphQLClient', () => {
    return {
        send: vi.fn(),
    };
});

describe('FeedAdministrationService', () => {
    describe('adminFeedFind', () => {
        test('should return feeds with pagination', async () => {
            const mockResponse = {
                data: {
                    adminRawFeedFind: {
                        totalCount: 40,
                        entities: [
                            {
                                _id: '42',
                                name: 'Jedi',
                                description: 'The jedi channel',
                                url: 'https://jedi.com/feed.xml',
                                icon: 'https://jedi.com/favicon.ico',
                                lastWatch: '2022-01-01T00:00:00.000Z',
                                lastETag: '1234567890',
                                error: null,
                            },
                        ],
                    },
                },
            };

            vi.mocked(send).mockReturnValueOnce(of(mockResponse as GraphqlResponse<unknown>));

            const params = { _p: 1, _pp: 20 };

            const actual = await lastValueFrom(adminFeedFind(params));

            expect(send).toHaveBeenCalledWith(
                expect.any(String), // The GraphQL query string
                params,
            );

            expect(actual.totalPage).toBe(2); // 40 total / 20 per page = 2 pages
            expect(await lastValueFrom(actual.data)).toEqual(mockResponse.data.adminRawFeedFind.entities);
        });

        test('should throw error when GraphQL request fails', async () => {
            // Mock an error response
            const mockError = new Error('GraphQL error');

            vi.mocked(send).mockReturnValueOnce(throwError(() => mockError));

            const params = { _p: 1, _pp: 20 };

            await expect(lastValueFrom(adminFeedFind(params))).rejects.toThrow('GraphQL error');

            expect(send).toHaveBeenCalled();
        });
    });

    describe('adminRawFeedDelete', () => {
        test('should delete feed and return success', async () => {
            // Mock successful deletion response
            const mockResponse = {
                data: {
                    adminRawFeedDelete: {},
                },
            };

            vi.mocked(send).mockReturnValueOnce(of(mockResponse as GraphqlResponse<unknown>));

            const idsToDelete = ['42', '66'];

            const actual = await lastValueFrom(adminRawFeedDelete(idsToDelete));

            expect(send).toHaveBeenCalledWith(
                expect.any(String),
                { _id: idsToDelete },
            );

            expect(actual).toBeUndefined();
        });

        test('should throw error when deletion fails', async () => {
            // Mock an error response
            const mockError = new Error('Failed to delete feed');

            vi.mocked(send).mockReturnValueOnce(throwError(() => mockError));

            const idsToDelete = ['42'];

            await expect(lastValueFrom(adminRawFeedDelete(idsToDelete))).rejects.toThrow('Failed to delete feed');

            expect(send).toHaveBeenCalled();
        });
    });

    describe('adminRawFeedGet', () => {
        test('should fetch a specific raw feed by ID', async () => {
            const mockResponse = {
                data: {
                    adminRawFeedGet: {
                        _id: '42',
                        name: 'Jedi',
                        description: 'The jedi channel',
                        url: 'https://jedi.com/feed.xml',
                        icon: 'https://jedi.com/favicon.ico',
                        lastWatch: '2022-01-01T00:00:00.000Z',
                        lastETag: '1234567890',
                        error: null,
                    },
                },
            };

            vi.mocked(send).mockReturnValueOnce(of(mockResponse as GraphqlResponse<unknown>));

            const idToFetch = '42';

            const actual = await lastValueFrom(adminRawFeedGet(idToFetch));

            expect(send).toHaveBeenCalledWith(
                expect.any(String), // GraphQL query string
                { _id: idToFetch },
            );

            expect(actual).toEqual(mockResponse.data.adminRawFeedGet);
        });

        test('should throw error when fetching raw feed fails', async () => {
            const mockError = new Error('Failed to fetch raw feed');

            vi.mocked(send).mockReturnValueOnce(throwError(() => mockError));

            const idToFetch = '42';

            await expect(lastValueFrom(adminRawFeedGet(idToFetch))).rejects.toThrow('Failed to fetch raw feed');

            expect(send).toHaveBeenCalled();
        });
    });

    describe('adminFeedUpdate', () => {
        test('should update feed details successfully', async () => {
            const mockResponse = {
                data: {
                    adminRawFeedUpdate: {
                        _id: '42',
                        name: 'Updated Jedi',
                        description: 'Updated description',
                        url: 'https://jedi.com/updated-feed.xml',
                        icon: 'https://jedi.com/updated-icon.ico',
                        lastWatch: '2022-01-02T00:00:00.000Z',
                        lastETag: '0987654321',
                        error: null,
                    },
                },
            };

            vi.mocked(send).mockReturnValueOnce(of(mockResponse as GraphqlResponse<unknown>));

            const idToUpdate = '42';
            const feedToUpdate = {
                _id: idToUpdate,
                name: 'Updated Jedi',
                description: 'Updated description',
                url: 'https://jedi.com/updated-feed.xml',
                icon: 'https://jedi.com/updated-icon.ico',
                lastWatch: new Date('2022-01-02T00:00:00.000Z'),
                lastETag: '0987654321',
            };

            const actual = await lastValueFrom(adminFeedUpdate(idToUpdate, feedToUpdate));

            expect(send).toHaveBeenCalledWith(
                expect.any(String), // GraphQL mutation string
                { _id: idToUpdate, rawFeed: feedToUpdate },
            );

            expect(actual).toEqual(mockResponse.data.adminRawFeedUpdate);
        });

        test('should throw error when update fails', async () => {
            const mockError = new Error('Failed to update feed');

            vi.mocked(send).mockReturnValueOnce(throwError(() => mockError));

            const idToUpdate = '42';
            const feedToUpdate = {
                _id: idToUpdate,
                name: 'New Name',
                description: 'New Description',
                url: 'https://new-url.com/feed.xml',
                icon: 'https://new-url.com/icon.ico',
                lastWatch: new Date('2022-01-02T00:00:00.000Z'),
                lastETag: '0987654321',
            };

            await expect(lastValueFrom(adminFeedUpdate(idToUpdate, feedToUpdate))).rejects.toThrow('Failed to update feed');

            expect(send).toHaveBeenCalled();
        });
    });

    describe('adminRawFeedScrap', () => {
        test('should scrap the feed header successfully', async () => {
            const mockResponse = {
                data: {
                    scrapFeedHeader: {
                        title: 'Jedi Channel',
                        description: 'The official Jedi feed',
                        icon: 'https://jedi.com/icon.png',
                        link: 'https://jedi.com/feed.xml',
                    },
                },
            };

            vi.mocked(send).mockReturnValueOnce(of(mockResponse as GraphqlResponse<unknown>));

            const linkToScrap = 'https://jedi.com/feed.xml';

            const actual = await lastValueFrom(adminRawFeedScrap(linkToScrap));

            expect(send).toHaveBeenCalledWith(
                expect.any(String), // GraphQL query string
                { link: linkToScrap },
            );

            expect(actual).toEqual({
                name: 'Jedi Channel',
                description: 'The official Jedi feed',
                icon: 'https://jedi.com/icon.png',
                url: 'https://jedi.com/feed.xml',
            });
        });

        test('should throw error when link is undefined', async () => {
            await expect(lastValueFrom(adminRawFeedScrap(undefined as unknown as string))).rejects.toThrow('Link is mandatory !');
        });

        test('should throw error when link is not a valid URL', async () => {
            const invalidLink = 'invalid-link';

            await expect(lastValueFrom(adminRawFeedScrap(invalidLink))).rejects.toThrow('Argument link must be a valid URL !');
        });

        test('should throw error when GraphQL request fails', async () => {
            const mockError = new Error('Failed to scrap feed header');

            vi.mocked(send).mockReturnValueOnce(throwError(() => mockError));

            const linkToScrap = 'https://jedi.com/feed.xml';

            await expect(lastValueFrom(adminRawFeedScrap(linkToScrap))).rejects.toThrow('Failed to scrap feed header');

            expect(send).toHaveBeenCalledWith(
                expect.any(String),
                { link: linkToScrap },
            );
        });
    });

});