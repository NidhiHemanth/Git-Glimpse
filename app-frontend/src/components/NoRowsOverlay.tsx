import React from 'react';

const NoRowsOverlay = () => {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      height: '100%',
      color: '#555',
      fontSize: '1.1rem',
      padding: '20px'
    }}>
      <img
        src="/no-data-icon.svg"
        alt="No data"
        style={{ width: '80px', marginBottom: '15px', opacity: 0.7 }}
      />
      <span>No Records Found</span>
    </div>
  );
};

export default NoRowsOverlay;
