// Club Page JavaScript - 从后端获取俱乐部数据

const API_BASE = 'http://localhost:8080/api';
let clubId = new URLSearchParams(window.location.search).get('id') || '1'; // 默认俱乐部ID为1

// 页面加载时获取俱乐部数据
document.addEventListener('DOMContentLoaded', () => {
  loadClubData();
  loadClubSchedule();
});

/**
 * 从后端获取俱乐部详细信息
 */
async function loadClubData() {
  try {
    const response = await fetch(`${API_BASE}/clubs/${clubId}`);
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const club = await response.json();
    
    // 更新页面上的俱乐部信息
    updateClubDisplay(club);
  } catch (error) {
    console.error('Failed to load club data:', error);
    showErrorMessage('Unable to load club information');
  }
}

/**
 * 更新页面显示的俱乐部数据
 * @param {Object} club - 俱乐部对象
 */
function updateClubDisplay(club) {
  // 更新俱乐部名称和描述
  document.querySelector('.crumbs span').textContent = club.name + ' Club';
  
  const clubName = document.querySelector('h2');
  if (clubName) clubName.textContent = club.name + ' Club';
  
  const description = document.querySelector('.card .pad p.muted');
  if (description) description.textContent = club.description || '暂无描述';
  
  // 更新标签信息
  const chipsContainer = document.querySelector('.chips');
  if (chipsContainer) {
    chipsContainer.innerHTML = `
      <span class="chip">🏸 ${club.sport || 'Sports'}</span>
      <span class="chip">📍 ${club.location || 'Location'}</span>
      <span class="chip">👥 ${club.members || 0} members</span>
      <span class="chip">🕒 Opening hours <span id="hoursLabel">${club.openingHours || 'N/A'}</span></span>
    `;
  }
  
  // 更新KPI数据
  const kpiContainer = document.querySelector('.kpi');
  if (kpiContainer) {
    kpiContainer.innerHTML = `
      <div class="k"><div class="muted">This week</div><div class="num">${club.eventsThisWeek || 0}</div></div>
      <div class="k"><div class="muted">Spots left</div><div class="num">${club.spotsLeft || 0}</div></div>
      <div class="k"><div class="muted">Rating</div><div class="num">${(club.rating || 0).toFixed(1)}</div></div>
      <div class="k"><div class="muted">Followers</div><div class="num">${club.followers || 0}</div></div>
    `;
  }
}

/**
 * 从后端获取俱乐部日程表
 */
async function loadClubSchedule() {
  try {
    const response = await fetch(`${API_BASE}/clubs/${clubId}/schedule`);
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    
    const schedule = await response.json();
    updateScheduleDisplay(schedule);
  } catch (error) {
    console.error('Failed to load club schedule:', error);
  }
}

/**
 * 更新日程表显示
 * @param {Array} schedule - 日程安排数组
 */
function updateScheduleDisplay(schedule) {
  // 这里可以根据实际的日程数据更新日程表显示
  // 具体实现取决于后端返回的日程数据结构
  console.log('Schedule loaded:', schedule);
}

/**
 * 显示错误消息
 * @param {string} message - 错误消息
 */
function showErrorMessage(message) {
  const errorDiv = document.createElement('div');
  errorDiv.className = 'error-message';
  errorDiv.textContent = message;
  errorDiv.style.cssText = `
    color: #dc2626;
    padding: 12px;
    background: #fee2e2;
    border-radius: 8px;
    margin-bottom: 16px;
  `;
  
  const container = document.querySelector('.wrap');
  if (container) container.insertBefore(errorDiv, container.firstChild);
}

/**
 * 处理"立即预订"按钮点击
 */
function handleBookNow() {
  // 实现预订逻辑
  console.log('Booking club:', clubId);
}

/**
 * 处理"添加到收藏"按钮点击
 */
function handleAddToFavourites() {
  // 实现添加到收藏的逻辑
  console.log('Adding to favourites:', clubId);
}

/**
 * 处理"联系管理员"按钮点击
 */
function handleContactAdmin() {
  // 实现联系管理员的逻辑
  console.log('Contacting admin for club:', clubId);
}
