const currentItems = [{id: 0, ten: 'Trắng'}, {id: 1, ten: 'Đen'}];
const item = {id: 10, ten: 'Màu mới'};
const result = [item, ...currentItems.filter(c => Number(c?.id) !== Number(item?.id))];
console.log(result);
