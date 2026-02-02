import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import './style.css'

// Pages
import Home from './pages/HomePage.vue'
import Login from './pages/LoginPage.vue'
import ClubHome from './pages/ClubHomePage.vue'
import ClubBookings from './pages/ClubBookingsPage.vue'
import VenueOverview from './pages/VenueOverviewPage.vue'
import Onboarding from './pages/OnboardingPage.vue'
import OnboardingLocation from './pages/OnboardingLocationPage.vue'
import Club from './pages/ClubPage.vue'
import Join from './pages/JoinPage.vue'
import User from './pages/UserPage.vue'
import ClubRegister from './pages/ClubRegisterPage.vue'
import ClubUpdates from './pages/ClubUpdatesPage.vue'

const routes = [
  { path: '/', component: Home, name: 'Home' },
  { path: '/home', redirect: '/' },
  { path: '/login', component: Login, name: 'Login' },
  { path: '/club-home', component: ClubHome, name: 'ClubHome' },
  { path: '/club-bookings', component: ClubBookings, name: 'ClubBookings' },
  { path: '/venue-overview', component: VenueOverview, name: 'VenueOverview' },
  { path: '/onboarding', component: Onboarding, name: 'Onboarding' },
  { path: '/onboarding-location', component: OnboardingLocation, name: 'OnboardingLocation' },
  { path: '/club', component: Club, name: 'Club' },
  { path: '/join', component: Join, name: 'Join' },
  { path: '/user', component: User, name: 'User' },
  { path: '/club-register', component: ClubRegister, name: 'ClubRegister' },
  { path: '/club-updates', component: ClubUpdates, name: 'ClubUpdates' },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const app = createApp(App)
app.use(router)

// Debug logs
console.log('✅ App created');
console.log('✅ Router configured with', routes.length, 'routes');

window.addEventListener('error', (e) => {
  console.error('❌ Runtime error:', e.message, e.filename, e.lineno);
});

try {
  app.mount('#app')
  console.log('✅ App mounted to #app');
} catch (e) {
  console.error('❌ Mount error:', e.message, e.stack);
  document.body.innerHTML = `<pre style="color:red">Error: ${e.message}\n${e.stack}</pre>`;
}
