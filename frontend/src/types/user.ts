import type { UserId } from "./ids";


export interface UserDto {

    id: UserId;

    handle: string;

    username: string;

    role: string;

}