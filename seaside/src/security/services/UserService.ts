import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { Page } from '@/common/model/Page';
import { ConstantFilters } from '@/constants';
import { User } from '@/security/model/User';
import { send } from '@/common/services/GraphQLClient';
import { UserListAdminResponse } from '@/security/model/UserListAdminResponse';

const DEFAULT_PER_PAGE: number = 20;
const DEFAULT_QUERY: string = `?${ConstantFilters.PER_PAGE}=${DEFAULT_PER_PAGE}&_s=login`;

const LOAD_USER_ADMIN_LIST_REQUEST = `#graphql
query LoadUsersAdminList ($_p: Int = 0, $_pp: Int = ${DEFAULT_PER_PAGE}, $_s: String = "login") {
    userSearch(_p: $_p, _pp: $_pp, _s: $_s) {
        totalCount
        entities {
            _id _createdAt _loginAt _loginIP login name mail roles
        }
    }
}`;

export function userList(page = 0, query: URLSearchParams = new URLSearchParams(DEFAULT_QUERY)): Observable<Page<User>> {
    const resolvedPage = (page > 0) ? page : 0;
    let resolvedPerPage = query.get(ConstantFilters.PER_PAGE);
    if (resolvedPerPage) {
        resolvedPerPage = String(DEFAULT_PER_PAGE);
    }
    return send<UserListAdminResponse>(LOAD_USER_ADMIN_LIST_REQUEST).pipe(
        map(data => data.data.userSearch),
        map(response => ({
                currentPage: resolvedPage,
                totalPage: Math.ceil(response.totalCount / Number(resolvedPerPage)),
                data: response.entities.map(user => ({
                    ...user,
                    _createdAt: utcToZonedTime(user._createdAt),
                    _loginAt: (user._loginAt)?utcToZonedTime(user._loginAt):null,
                })),
            }),
        ),
        take(1),
    );
}

function utcToZonedTime(utcDate: string): string {
    return new Date(utcDate).toLocaleString();
}

const USER_CREATE_REQUEST = `#graphql
mutation CreateNewUser ($user: UserForm) {
    userCreate(user: $user) {
        _id _createdAt login name mail roles
    }
}`;

export function userCreate(user: User): Observable<User> {
    return send<{ userCreate: User }>(USER_CREATE_REQUEST, { user: user }).pipe(
        map(data => data.data.userCreate),
        take(1),
    );
}

const USER_DELETE_REQUEST = `#graphql
mutation DeleteUsers ($ids: [ID]) {userDelete(ids: $ids) {_id}}`;

export function userDelete(ids: string[]): Observable<User[]> {
    return send<{ userDelete: User[] }>(USER_DELETE_REQUEST, { ids: ids }).pipe(
        map(data => data.data.userDelete),
        take(1),
    );
}

const USER_UPGRADE_REQUEST = `#graphql
mutation UpdateNewUser($id:ID , $currentPassword: String, $user:UserForm){
    userUpdate(_id: $id, currentPassword: $currentPassword, user: $user){
        _id _createdAt login name mail roles
    }
}`;

export function userUpdate(id: string, user: Partial<User>, currentPassword: string | null = null): Observable<User> {
    const { _id, _createdAt, _loginAt, ...toUpdate } = user;
    const variables = { id: id, currentPassword: currentPassword, user: { ...toUpdate } };
    return send<{ userUpdate: User }>(USER_UPGRADE_REQUEST, variables).pipe(
        map(data => data.data.userUpdate),
        take(1),
    );
}
