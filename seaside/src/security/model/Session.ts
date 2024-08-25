import { User } from '@/security/model/User';
import { UserSettings } from '@/security/model/UserSettings.type';

export type Session = {
    user: User;
    settings: UserSettings,
    maxAge: number;
}