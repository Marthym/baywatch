import { Observable } from 'rxjs';
import { send } from '@/common/services/GraphQLClient';
import { map, take } from 'rxjs/operators';
import { UserSettings } from '@/security/model/UserSettings.type';

const USERS_SETTINGS_GET_REQUEST = `#graphql
query UserSettingsGet($id: ID){
    userSettingsGet(userId: $id) {
        preferredLocale autoread
    }
}`;

export function userSettingsGet(userId: string): Observable<UserSettings> {
    return send<{ userSettingsGet: UserSettings }>(USERS_SETTINGS_GET_REQUEST, { id: userId }).pipe(
        map(data => data.data.userSettingsGet),
        take(1),
    );
}

const USERS_SETTINGS_UPDATE_REQUEST = `#graphql
mutation UserSettingsUpdate($id: ID, $settings: UserSettingsForm){
    userSettingsUpdate(userId: $id, settings: $settings) {
        preferredLocale autoread
    }
}`;

export function userSettingsUpdate(userId: string, settings: UserSettings): Observable<UserSettings> {
    const variables = {
        id: userId,
        settings: settings,
    };
    return send<{ userSettingsUpdate: UserSettings }>(USERS_SETTINGS_UPDATE_REQUEST, variables).pipe(
        map(data => data.data.userSettingsUpdate),
        take(1),
    );
}