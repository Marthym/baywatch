import { User } from '@/security/model/User';

type UserPage = {
    totalCount: number
    entities: User[]
}
export type UserListAdminResponse = {
    userSearch?: UserPage
}
