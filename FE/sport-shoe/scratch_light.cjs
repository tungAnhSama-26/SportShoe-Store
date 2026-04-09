const fs = require('fs');

const cssPath = 'src/theme/index.css';
let css = fs.readFileSync(cssPath, 'utf8');
css = css.replace(/background: #09090b;/, "background: #111827;");
css = css.replace(/color: #e4e4e7;/, "color: #f3f4f6;");
fs.writeFileSync(cssPath, css);

const layoutPath = 'src/layouts/admin/AdminLayout.vue';
let layout = fs.readFileSync(layoutPath, 'utf8');
layout = layout.replace(/dark:bg-zinc-950/g, 'dark:bg-gray-900');
layout = layout.replace(/dark:text-zinc-200/g, 'dark:text-gray-100');
fs.writeFileSync(layoutPath, layout);

const headerPath = 'src/layouts/admin/HeaderAdmin.vue';
let header = fs.readFileSync(headerPath, 'utf8');
header = header.replace(/dark:bg-zinc-900/g, 'dark:bg-gray-800');
header = header.replace(/dark:bg-zinc-800/g, 'dark:bg-gray-700');
header = header.replace(/dark:bg-zinc-700/g, 'dark:bg-gray-600');
header = header.replace(/dark:text-zinc-([0-9]+)/g, 'dark:text-gray-$1');
fs.writeFileSync(headerPath, header);

const sidebarPath = 'src/layouts/admin/SidebarAdmin.vue';
let sidebar = fs.readFileSync(sidebarPath, 'utf8');
sidebar = sidebar.replace(/dark:bg-zinc-900/g, 'dark:bg-gray-800');
sidebar = sidebar.replace(/dark:border-zinc-800\/50/g, 'dark:border-gray-700');
sidebar = sidebar.replace(/dark:hover:bg-zinc-800\/50/g, 'dark:hover:bg-gray-700/50');
sidebar = sidebar.replace(/dark:text-zinc-([0-9]+)/g, 'dark:text-gray-$1');
fs.writeFileSync(sidebarPath, sidebar);
