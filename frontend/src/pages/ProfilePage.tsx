import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import './ProfilePage.css';

const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'profile' | 'stats'>('profile');

  const renderProfileTab = () => (
    <div className="profile-details">
      <div className="profile-field">
        <span className="field-label">Username:</span>
        <span className="field-value">{user?.username}</span>
      </div>
      <div className="profile-field">
        <span className="field-label">Email:</span>
        <span className="field-value">{user?.email}</span>
      </div>
      <div className="profile-field">
        <span className="field-label">User ID:</span>
        <span className="field-value">{user?.id}</span>
      </div>
    </div>
  );

  const renderStatsTab = () => (
    <div className="profile-stats">
      <div className="stats-box">
        <h3>Activity Summary</h3>
        <p>This feature will be available soon!</p>
        <p>In the future, you'll be able to see statistics about your wishes, such as:</p>
        <ul>
          <li>Total wishes created</li>
          <li>Wishes completed</li>
          <li>Completion rate</li>
          <li>Average completion time</li>
        </ul>
      </div>
    </div>
  );

  return (
    <div className="profile-page">
      <h1>Your Profile</h1>
      
      <div className="profile-tabs">
        <button
          className={`tab-button ${activeTab === 'profile' ? 'active' : ''}`}
          onClick={() => setActiveTab('profile')}
        >
          Profile Information
        </button>
        <button
          className={`tab-button ${activeTab === 'stats' ? 'active' : ''}`}
          onClick={() => setActiveTab('stats')}
        >
          Statistics
        </button>
      </div>
      
      <div className="tab-content">
        {activeTab === 'profile' ? renderProfileTab() : renderStatsTab()}
      </div>
      
      <div className="coming-soon">
        <h3>Coming Soon</h3>
        <p>Profile editing functionality will be available in future updates!</p>
        <ul>
          <li>Change username</li>
          <li>Update email</li>
          <li>Change password</li>
          <li>Set profile preferences</li>
        </ul>
      </div>
    </div>
  );
};

export default ProfilePage; 