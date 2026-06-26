import { Routes } from '@angular/router';
import { CardsList } from './cards/cards-list';
import { Login } from './auth/login';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: '', component: CardsList }
];
