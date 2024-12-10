import {types} from "mobx-state-tree";

const User = types
    .model('User', {
        name: ''
    })
    .views((self) => ({
        get isAuthenticated() {
            return !!self.name;
        }
    }))
    .actions((self) => {
        return {
            login(username: string, token: string) {
                self.name = username;
                localStorage.setItem('Authorization', username);
                localStorage.setItem('ws_token', token);
            },
            logout() {
                localStorage.setItem('Authorization', '');
                localStorage.removeItem('ws_token');
                self.name = '';
                console.log("logout finished!")
            },
            afterCreate() {
                self.name = localStorage.getItem('Authorization') || '';
            }
        }
    });

export default User;
