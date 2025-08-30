import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import { TestScheduler } from 'rxjs/testing';
import { firstValueFrom, of, throwError } from 'rxjs';
import { send } from '@/common/services/GraphQLClient';
import { UnauthorizedError } from '@/common/errors/UnauthorizedError';
import { UnknownFetchError } from '@/common/errors/UnknownFetchError';
import { ForbiddenError } from '@/common/errors/ForbiddenError';
import { ValidationError } from '@/common/errors/ValidationError';
import { BadRequestError } from '@/common/errors/BadRequestError';
import {
    BAD_REQUEST,
    FORBIDDEN,
    INTERNAL_ERROR,
    INVALID_SYNTAX,
    UNAUTHORIZED,
    VALIDATION_ERROR,
} from '@/common/model/GraphqlResponse.type';
import { fromFetch } from 'rxjs/fetch';

describe('GraphQLClient', () => {
    let testScheduler: TestScheduler;
    let mockFromFetch: any;
    let mockHandleStatusCodeErrors: any;

    beforeEach(() => {
        testScheduler = new TestScheduler((actual, expected) => {
            expect(actual).toEqual(expected);
        });

        const mocks = vi.hoisted(() => {
            return {
                fromFetch: vi.fn(),
            };
        });

        vi.mock('rxjs/fetch', () => ({
            fromFetch: mocks.fromFetch,
        }));
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    describe('gqlMinify', () => {
        test('should_minify_current_user_query', async () => {
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: { currentUser: { _id: '648000000000000000000000', login: 'john.doe', mail: '', name: '' } },
                }),
            }));
            const query = `#graphql
            query CurrentUser {
                currentUser {
                    _id login mail name
                }
            }`;
            expect(await firstValueFrom(send(query))).toEqual({
                data: {
                    currentUser: {
                        _id: '648000000000000000000000',
                        login: 'john.doe',
                        mail: '',
                        name: '',
                    },
                },
            });
            expect(fromFetch).toHaveBeenCalledWith(
                expect.any(String),
                expect.objectContaining({
                    body: expect.stringContaining('"query":"query CurrentUser{currentUser{_id login mail name}}"'),
                }),
            );
        });
    });

    describe('send', () => {
        test('should_send_successful_jedi_council_query', async () => {
            const query = 'query JediCouncil { jedis { name rank } }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: { jedis: [{ name: 'Yoda', rank: 'Master' }, { name: 'Mace Windu', rank: 'Master' }] },
                    errors: [],
                }),
            }));

            expect(await firstValueFrom(send(query))).toEqual({
                data: { jedis: [{ name: 'Yoda', rank: 'Master' }, { name: 'Mace Windu', rank: 'Master' }] },
                errors: [],
            });

            expect(fromFetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/g'),
                expect.objectContaining({
                    method: 'POST', headers: expect.any(Headers),
                    body: JSON.stringify({ query: 'query JediCouncil{jedis{name rank}}', variables: undefined }),
                }),
            );
        });

        test('should_handle_unauthorized_error_like_sith_infiltration', async () => {
            const query = 'query SithSecrets { darkSide }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: null, errors: [{
                        message: 'Access denied to the dark side',
                        extensions: { classification: UNAUTHORIZED, translation: 'sith.access.denied' },
                    }],
                }),
            }));

            await expect(() => firstValueFrom(send(query))).rejects.toThrowError(
                new UnauthorizedError('sith.access.denied', 'Access denied to the dark side'),
            );
        });

        test('should_handle_unauthorized_error_without_translation_like_rebel_spy', async () => {
            const query = 'query RebelPlans { plans }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: null,
                    errors: [{
                        message: 'Rebel spy detected',
                        extensions: { classification: UNAUTHORIZED },
                    }],
                }),
            }));

            await expect(() => firstValueFrom(send(query))).rejects.toThrowError(
                new UnauthorizedError('Rebel spy detected', 'Rebel spy detected'),
            );
        });

        test('should_handle_forbidden_error_like_jedi_archives_restricted', async () => {
            const query = 'query JediArchives { ancientSecrets }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: null,
                    errors: [{
                        message: 'Access to Jedi Archives restricted to Council members only',
                        extensions: { classification: FORBIDDEN },
                    }],
                }),
            }));

            await expect(() => firstValueFrom(send(query))).rejects.toThrowError(
                new ForbiddenError('Access to Jedi Archives restricted to Council members only'),
            );
        });

        test('should_handle_validation_error_like_invalid_lightsaber_crystal', async () => {
            const query = 'mutation CreateLightsaber($crystal: String!) { createLightsaber(crystal: $crystal) }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: null,
                    errors: [{
                        message: 'Invalid lightsaber crystal configuration',
                        extensions: {
                            classification: VALIDATION_ERROR,
                            properties: ['crystal.type', 'crystal.purity'],
                        },
                    }],
                }),
            }));

            await expect(() => firstValueFrom(send(query, { crystal: 'corrupted-synthetic' }))).rejects.toThrowError(
                new ValidationError('Invalid lightsaber crystal configuration', ['crystal.type', 'crystal.purity']),
            );
        });

        test('should_handle_validation_error_without_properties_like_droid_malfunction', async () => {
            const query = 'mutation RepairDroid($droidId: ID!) { repairDroid(id: $droidId) }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: null,
                    errors: [{
                        message: 'C-3PO protocol droid validation failed',
                        extensions: {
                            classification: VALIDATION_ERROR,
                        },
                    }],
                }),
            }));

            await expect(() => firstValueFrom(send(query, { droidId: 'c3po' }))).rejects.toThrowError(
                new ValidationError('C-3PO protocol droid validation failed', []),
            );
        });

        test('should_handle_bad_request_error_with_translation_like_invalid_hyperdrive_coordinates', async () => {
            const query = 'mutation HyperdriveJump($coordinates: String!) { jump(to: $coordinates) }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: null,
                    errors: [{
                        message: 'Invalid hyperdrive coordinates provided',
                        extensions: { classification: BAD_REQUEST, translation: 'hyperdrive.coordinates.invalid' },
                    }],
                }),
            }));

            await expect(() => firstValueFrom(send(query, { coordinates: 'unknown-system' }))).rejects.toThrowError(
                new BadRequestError('hyperdrive.coordinates.invalid', 'Invalid hyperdrive coordinates provided'),
            );
        });

        test('should_handle_bad_request_error_without_translation_like_death_star_construction_error', async () => {
            const query = 'mutation BuildDeathStar($blueprint: String!) { construct(blueprint: $blueprint) }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: null,
                    errors: [{
                        message: 'Death Star construction parameters invalid',
                        extensions: { classification: BAD_REQUEST },
                    }],
                }),
            }));

            await expect(() => firstValueFrom(send(query, { blueprint: 'flawed-design' }))).rejects.toThrowError(
                new BadRequestError('Death Star construction parameters invalid', 'Death Star construction parameters invalid'),
            );
        });

        test('should_handle_syntax_error_like_corrupted_hologram_transmission', async () => {
            const query = 'query CorruptedHologram';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: null,
                    errors: [{
                        message: 'Hologram transmission corrupted',
                        extensions: { classification: INVALID_SYNTAX },
                    }],
                }),
            }));

            await expect(() => firstValueFrom(send(query))).rejects.toThrowError(
                new UnknownFetchError('Application fail to fetch server !'),
            );
        });

        test('should_handle_internal_server_error_like_death_star_reactor_overload', async () => {
            const query = 'query ReactorStatus { reactor { temperature } }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: null,
                    errors: [{
                        message: 'Death Star reactor core overload detected',
                        extensions: {
                            classification: INTERNAL_ERROR,
                        },
                    }],
                }),
            }));

            await expect(() => firstValueFrom(send(query))).rejects.toThrowError(
                new UnknownFetchError('An error occurred on the server side !'),
            );
        });

        test('should_pass_through_response_without_errors_like_successful_jedi_training', async () => {
            const query = 'query JediTraining { padawan { level } }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: { padawan: { level: 'Knight' } },
                    errors: [],
                }),
            }));

            expect(await firstValueFrom(send(query))).toEqual({
                data: { padawan: { level: 'Knight' } },
                errors: [],
            });
        });

        test('should_handle_response_with_null_errors_like_empty_tauntaun_belly', async () => {
            const query = 'query TauntaunStatus { tauntaun { warmth } }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: { tauntaun: { warmth: 'frozen' } },
                    errors: null as any,
                }),
            }));

            expect(await firstValueFrom(send(query))).toEqual({
                data: { tauntaun: { warmth: 'frozen' } },
                errors: null,
            });
        });

        test('should_handle_multiple_errors_and_prioritize_unauthorized_like_sith_lord_infiltration', async () => {
            const query = 'query SithAndJediSecrets { secrets }';
            vi.mocked(fromFetch).mockReturnValue(of({
                ok: true, status: 200,
                json: () => Promise.resolve({
                    data: null,
                    errors: [
                        { message: 'Validation failed', extensions: { classification: VALIDATION_ERROR } },
                        {
                            message: 'Sith Lord detected in Jedi Temple',
                            extensions: { classification: UNAUTHORIZED, translation: 'sith.infiltration.detected' },
                        },
                        { message: 'Bad request', extensions: { classification: BAD_REQUEST } },
                    ],
                }),
            }));

            await expect(() => firstValueFrom(send(query))).rejects.toThrowError(
                new UnauthorizedError('sith.infiltration.detected', 'Sith Lord detected in Jedi Temple'),
            );
        });

        test('should_propagate_network_errors_like_communication_disruption_with_rebel_base', async () => {
            const query = 'query RebelBaseStatus { base { operational } }';
            const networkError = new Error('Communication with rebel base lost');
            vi.mocked(fromFetch).mockReturnValue(throwError(() => networkError));

            await expect(() => firstValueFrom(send(query))).rejects.toThrowError(networkError);
        });
    });
});