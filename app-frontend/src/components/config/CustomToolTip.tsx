export const CustomTooltip = (props: any) => {
  const { value } = props;
  return (
    <div style={{ backgroundColor: 'lightgray', padding: '5px' }}>
      <p>{value}</p>
    </div>
  );
};
