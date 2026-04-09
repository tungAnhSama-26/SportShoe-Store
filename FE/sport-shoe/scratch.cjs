const fs = require('fs');

const cssPath = 'src/theme/index.css';
let css = fs.readFileSync(cssPath, 'utf8');
css = css.replace(/radial-gradient.+rgba\(207, 16, 24, 0\.1\).+?linear-gradient.+?#0f172a 0%, #1e293b 100%\);/s, 
  "#09090b;\n    /* Remove intense gradient for dark mode and use neutral black */");
css = css.replace(/color: #e2e8f0;/, "color: #e4e4e7;");
fs.writeFileSync(cssPath, css);

const layoutPath = 'src/layouts/admin/AdminLayout.vue';
let layout = fs.readFileSync(layoutPath, 'utf8');
layout = layout.replace('dark:bg-slate-900', 'dark:bg-zinc-950');
layout = layout.replace('dark:text-gray-100', 'dark:text-zinc-200');
fs.writeFileSync(layoutPath, layout);

const headerPath = 'src/layouts/admin/HeaderAdmin.vue';
let header = fs.readFileSync(headerPath, 'utf8');
header = header.replace(/dark:bg-slate-800/g, 'dark:bg-zinc-900');
header = header.replace(/dark:bg-slate-700/g, 'dark:bg-zinc-800');
header = header.replace(/dark:bg-slate-600/g, 'dark:bg-zinc-700');
header = header.replace(/dark:text-gray-200/g, 'dark:text-zinc-200');
header = header.replace(/dark:text-gray-300/g, 'dark:text-zinc-300');
header = header.replace(/dark:text-gray-400/g, 'dark:text-zinc-400');
header = header.replace(/dark:text-gray-100/g, 'dark:text-zinc-100');
fs.writeFileSync(headerPath, header);

const sidebarPath = 'src/layouts/admin/SidebarAdmin.vue';
let sidebar = fs.readFileSync(sidebarPath, 'utf8');
sidebar = sidebar.replace(/dark:bg-slate-800/g, 'dark:bg-zinc-900');
sidebar = sidebar.replace(/dark:border-slate-700/g, 'dark:border-zinc-800\/50');

// add adaptive hover/active states for sidebar links
sidebar = sidebar.replace(/:class="isActive\('\/admin\/[a-z-]+'\) \? 'bg-red-50 text-red-600 font-semibold' : 'text-gray-700 hover:bg-gray-50'"/g, 
  (match) => match.replace("'bg-red-50 text-red-600 font-semibold' : 'text-gray-700 hover:bg-gray-50'", "'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-zinc-300 hover:bg-gray-50 dark:hover:bg-zinc-800/50'"));

// submenu items
sidebar = sidebar.replace(/:class="isActive\('\/admin\/[a-z-]+'\) \? 'bg-\\[#ffcfd2\\] text-\\[#e0484d\\] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'"/g, 
  (match) => match.replace("'bg-[#ffcfd2] text-[#e0484d] border-red-100 shadow-sm' : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'", "'bg-[#ffcfd2] dark:bg-red-500/10 text-[#e0484d] dark:text-red-400 border-red-100 dark:border-red-500/20 shadow-sm' : 'text-gray-500 dark:text-zinc-400 hover:text-gray-800 dark:hover:text-zinc-200 hover:bg-gray-50 dark:hover:bg-zinc-800/50'"));

// submenu item icons
sidebar = sidebar.replace(/'text-red-500' : 'text-gray-400'/g, "'text-red-500' : 'text-gray-400 dark:text-zinc-500'");
sidebar = sidebar.replace(/'text-red-500' : 'text-gray-500 group-hover:text-gray-700'/g, "'text-red-500' : 'text-gray-500 dark:text-zinc-500 group-hover:text-gray-700 dark:group-hover:text-zinc-300'");

// open links bindings
sidebar = sidebar.replace(/\? 'bg-red-50 text-red-600 font-semibold' : 'text-gray-700 hover:bg-gray-50'/g, 
  "? 'bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 font-semibold' : 'text-gray-700 dark:text-zinc-300 hover:bg-gray-50 dark:hover:bg-zinc-800/50'");

fs.writeFileSync(sidebarPath, sidebar);
