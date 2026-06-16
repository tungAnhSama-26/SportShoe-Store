const q="data:image/webp;base64,UklGRmwXAABXRUJQVlA4TGAXAAAvr8RKEIfCqG0jQWFzPI/31M+lwKCRJEXr39N54nsYQtC2bZp0+8+fwKkyCgCQkb0FEEoFEXSSQSXfve5561AF/MlfZVYZD2gpwBJYgBLom5dBAkpgdJM4gwicGShBSRITBLqhlCD+zoxjKpSh1N+Bcijf3QsKqEDFznmoRMX2+YiwdROpbp2b1HvW+5tv3s3YGw9cf+DZ/tqP8K1t2+Umsm3pVygkRUghqSxyBa6iaBStkW6sGjQ1XGpqVrBeQzv/wwEbO/7vfd/vExmtZUT/ZdG2FbcNHK/k9SUnQ10qGw2gKD+D3lX90//yp//l/2CVLx/P1X5U+fTu1Yv2O/Xlm/dfek65Z+0V6ov3X/pLuSvXZ2/7Rq98fv/6Qq7TK32i3PM2S33+sTeUy1jf9IFyuevLL/2enEl98aWHlOv8wT71dXKm9HBy9vRrchh87vXkekeyLx/enN9mGo6eSw6T/koOmF5KDp2+SY6CHkmOh35IjozeR46RPkeuH8VHitzu4YMHm14IH9++bBnq/lb6Vn952oU+RY6hnB2nC/V+F3oTOY7Xv3NL6aQLvYcc2a3Hwad3r1q6eppSJl72FA74TFk32XjdT8j1ovj8/kJOIX2BnScrqCe9CD7/18VcH4ovH76+7bVvG94I5g/87RC3KYnmj/t1UO8n2fxhv67Ez0k3f9CvK3GWztWnBymrRkQ1/sasYIvNN3DZqSyMvhDue5rsdSUeny9Hu0OSVVNNy1GSUUddyjzT5z1dIJZ5Mql1pzI0+isBK7Nx3i++kL1e1+10vq6l8ai9cn0XkLKox0USU6suZZpy1bLBK1Ob0qm9XiSjCvWizt5ST9WtLNGhpiSPjk/rvQ/G8tyF/9mRC1zrUhD/qx5dLDBLQpVVqYH+ezT4Rs5V7+j9NMknFH+SNNVhl2/VMuWs5QqrLJNJmXRtd7nDhcX/j7IPSnEvyeL3YNFMEkWFa11q4ueYZt8K1JLQKiWzkPc2ln0AnetJFvvjQLGaFUlYnXUp49yLVVBlbFNWHdtds5D2lmqdB/u6gvXwY1URPVkG17oMiZ/pGcI9ZjRcqh7xPgm4UH1gwk3ygZC8JtHa5H+eHehtGrj+1UGRqMPaps9C9kFUb6dDj9lVXlNqJX7sVCFamcOVzV1vIvuAz+t02NGMkr66gHr4VHBbh92WrCzLmLbPQvXB6XcpHXKsLlvZUq91RfxkzwrPWto+r8jaZ6F6IA0P05XqL/sDiaZICssEbHmkorbOsliNvh77kkxKKXvQHzfS1eqxZj66/qeirZPEu7LSBG5JaG5bxqRbYKoeoMw+JcE8FT+fAcztPnRbTRZHhFltQ84tMFUPpug0KWa964DT3ZG6rYvEWxaIVuqwUGcFG/jV0ySD3cN//9eHZ4Hie2+m3FrZFF1W45ByC0zdA2nYiGB/58JoaPZBYjVJOkvTqS+PuJSIVuawlmX99O78oD/E070ZPD4LEauLDn+tKU10Wa1Dh6wfXgMf8Pm+Ak5ytIBn63wcFrc0hutOm2GUOaLVPPTG+vk1+NHpT/jZ5GkBdxlx8+ayNaWVLCtz2Oixfjw39ivf+DWhcUjc0jJHdxrjrrcKRKt9yGdFf809dms4XLw5bU1TWVbqcKTF+vnbuPrYaTVzMNzSFK47rcEoNbzVptPTC+vbtm35OdHMF7/KJIktDVp32lCklWy9hpkS66cXbRseztwdWumU+gbXusBv4Uf1FZcYW6N7YH1HMGS17V4/x3vRWP8n7rm1lmWFCMm2wMTcNPF1S1BvHyvj170rgB0rygvrFP4YNnhfcUcrRsi1P2nIG8ebpSSNYwC8MrhuLXRZMULx1k/PSYat7R4+vZEb1iW8leorLhJISLU/acQbyWhr755vAQ8nmqS3TPG604itTcd2FyVksqq+tT8nPWxTZFgVX4szVtYDt48RrSgh0/6kVd92KQlik42/eMw0CS5w3WmlKOs4JeZwYmQFtJGUx0k5m+Qvi683T6yNXGvHdhcnlG3lWTv1djqsKBWXGq87TZaVOVxIsL5uaUpK+XmoGU/WUHXFOnLEOk2JOawUWN8zTcLLgo1kXuvpXqxsnrcoFJcCrztNlRWqW0Sy9fMznnIvCcV8ijtacmVKhdXLQ8tKslrbSoH13LwsqazpavPe3R+roTVHR87yjlpILnO87jSxVpsH2oqt74huZ1doAZVxYo+E3HeXT4xehtE65LEaPaOl1/rlGVF5nHyFPQdwRy2SM9YFvJVq/Q20R+4cVjwjkdVZiHMYd9R5Ss5YK1+sWKFc6+eWqaTkLZw5nDtqibDF83T2dQoPbBXOKuornsIN0Uut9SVT2SZ/ocsZ3FGR+77K2UrNFpi+WKnDkt76ierl/SSQ+3Aw5WzuqMALD5OlE+vczoi/4nlHK1oo1vqaqlxP1kiZu879fJDkMuz6nO2tx8Sv+WFNXa3M4dzICpbnsnrCST7wc5nuqFxvXbBM/YqjO02XlTmc0E+T5y1XWduzg/kGPLr5t7sbHqBzyHfUieV7rRzZw0Uly0odFvRWspmG77rwNBNrEJ4cnR+n6ZaF96g59Dvq0HRNBk2ldsTapIQf4h2XG+ytVXC0QyRD+7bJ1QI+yTbrORoWVZn/jsoUz1zZI89QlxU/xDsuN1J50+pgzcOJxSLbRsgkwha1zZTh5hbvjXCLq3WUEEOh1hetv/xLSpQg5Vh2fV54cxvMfbFChtj/3YzB3lsXP8GySaTg5FjuqEvrfaLE1joD3XxKpvV9K4zd2husc5j/CgD/MY4GwkrhqtXmWyzT+qqNABsaUHIsd9Sx6R5RomFdkExsHOGuqdL6rHUO66btVq5P7/d8gORYNhYeGg7MIB7WCtEKGoq0fmzb0LA/zvbpQbKcTQr8Q6/A/WMcfls/NcZW0DAg1n0IwMiRWEW/9GTw3F2tzOHYVevjS55Q/2di5w4rADmW7rWR2RTVgmylDn213kyXsLHn+t54KfOYFfuc9a7P0dfuUVamcgfPTbkv/1E4rPuUMnKWiWPRGOU0WmeDOFrLrlbmcOqq9fR805aJG3tl7OynaWicY+lem7gTL32xAofRsN5NWTnmZJtr7jrmPPtkmjPqLuRZGAmkddFxFGDM4dLXfUL8nPSx3tGQK8dsXWh9b+DuNF2naQY5RLWq3kXgLiVJXAPjRj6+m2MoM5pxZEgrzliRw2BYky1PEyq7NRa7dS7+53+75VRbh4MYW1fg402VZ/3CZjVm4wdnm7s/s416lmalTn+sS1lW7DAU+4RIST3XrLhijqxUh6V1gn5MbnXW12TnXls+u7UVuXKHiHWqyor+ECAS1nvJdfan964Tz+GGZjHIH2sja2Lj6GEorOC0ynhy/zr5OGEOTit6qG0qYG/ZDtAKzokYcs3hRpe1joF1rMvKHI5tinCrr/xFy6hnDUrpjrXxZGLj5PfSoavW291bwEe+sOk4h5sfPFb8UNnExj+05MPNyjnZpjstI7/sDdk+vClv1LOYa/dIKyO/JzZusxwWBes22XJCyXE+qHJG3WuHl7VCshrdgYRNbPw52XCzPCNTLsBWUYPnprcaLaAHwXo9QXEih+65g8yaRB1bo+K4o+qyvsMv+Zo2LvaPb/7yt9/P0HhwU9qoZyNgHTpiLUm2ypdlfdl6zPbCofWf2NN1DjeHoLWWZeUIhVkdZvO9z85NClUrdS6aed1cXusqW819jtynRktX3/0y6yZHnVVXqiXiaZrhCHVZHWZzSQtozH92m8ONy1b+szLdqVBP0hzRShLKsqoEadZzu6MEwu7xbQfmcEO8ds+qGvJNbFxsqRTMcmMmy+oYmw5nmtSIS3Pf3kNo0WNtCr7WZcZcEK0soSbr55ay/p2djRl5cpqtjP8easbWhfqUwCNOBYwm9MVqfdwulVz26d3qnnPGir3u/pSydRk68h9byb0nkQm99VUrh8ekZPr0/nHz0pxr1jGAlfIh4UTUSXvQYB0ZNdaKrIO21cMm27P4LHStQbMuh5ynaUbuf2x4HcYFSrjw1XpqQ65P79icW61vTBitrANOCIx1WaHckSp66xuyA7RuMoH36dnONufs3zqVqFmXnK3LSu6Up/E6jK3+tsfOWx8mMZx0Z/f49s9eBCtGK+cjwqkj1gZvAJB2AwgD2XWkbOuNJI5HV1ivy49airEWnK3LxHtrY/b5zly37lPi5X7mIUiebe52yoXGmi8OuHVoOOI5kF1HyrN2fcp8y8mts0xcOIiqP3WhxVqTTmxcrhWvw9jyBPdBDOtJ/HCz0nrHydGTPPzj0lxMrWPO1qVy32o5qAEx1o9sB2jNzVMw0o1tnk0ofSoTMQfcSiGyInYYG4YQw3qSbbWYRRMa6cZm7zQzLdY58uJraKwDyxDAKvwArQJI67unl//B2W+0xZ/hZhWhtCJ2GFuGfltvdmgBr8HRtV6/93i73X973+3pg9s/e/hy5KZ1Hhmr7TaHUqzvuazdZiCAxZm8dSD8Gm7WnN6aPLHidRhbrwXhtfU0YfNPpINPhM2aImmtcTuM5+Zh4awVm/VOOooGz63ZWgfGWlvfofy2aiKS1jqQ1gKwwxjmgWRtZBU+3CwMztZw/Hi1Wv2NoISls1YUdnj8iLTOETuMrUNjq/DhZonih6Q1Qf6EcUDCuavWNiVCDm3rykmrVbtrH9padQ83K+40Wqwz4FJ6YsXtMLbcdtFj6/3EyY9aK2yHse22ixqsn9hmM87JUQTKmNLK+YhwESCr7baLMqxkoX0LeGbBJmbWCtnaxMiK12EM8bdQahjW00u66x0lH+YzXL22DwZLxVa47vp5AKwI4VKEVQc/XdICgs2/cBMw6wraOgqRFbDDGOOTrxUMUvpDy0iupi2WTP20ijrFrhomNo5yFH0N1qDi9uC5DxsrSCjhpNPpZ5tyEWFrKc1ay93pEGKHMUjIP0jpT23rAJtsxMq68Mdaem9doDwbMOK3vmsPY2oW6yKQ1glehzFOb8tIgNVXfgRb7VZkwHlk7qv1lJOTNgIULNYJv3XmiHWF12GMM+BdX63XThRwCFhXMbICdhhzbxi1En6AVgWc+FzmXlpHgbGWQF21rlofJkDaKCHGumyy1Nqoux7OOmsg6gRv8Nxm7S5zWEkfblY6acGmrmrA/jiyVmmngadUe0LwITuMUUJXrfvMC1l/bbG4tf/OLbRWqFI68pMTl2GFCY2suoeblZkTEx6nq9dfduefursRiwO0oi4GqWld+Hd1PpNhhQldHTz33WTKfYAnxe7v2t3vwbVClbn7PzlxnnZ3FBrr2nraX6TdjjEbPDdz6xKY0zSzwhkEvbOD504SiK3VoMC1a46cdLqKqN1dUFiFH6BVMTEbPLem1oV54+BahJV79d2p8OFmhZ2SZmLjAqyO/OTEITuMmcNG+HCznCNkExuHa9c8Oel0VB3Gy7hYT1PcWSie2HhkrgMJwBZ/hG8VPtwsBRy6Vk9O0wxXi1/jW4UPN4uL4wPPOo3mdSCxGnQLo2I9M20B71uwjwQzN62unKYZshZ/FBVrjnkv3bKff+H9SzjkrMtoXgcSVefvDbrVl+FmpbRBmzBmSBhJtnp/HUjQdRgvY2Jt1wmJO7GiommmsKzOXwcSE8QOY+pw4MpwswimMxc16zgG1toTa/dSBcT68u1/j8LOkKaZCop1KsKK87wD/3CzeHIfz18IFZzciMBws6JmLVyx5vj2iLS+JcoNiNmt4zx4bqyT2ufIdSAhxsq57WIh1Prs1bvzl3xM+FlGwCr3JyfO2GG8EGmlyIkgyIPnhiozT64DCRlW5nAOtUtT+NyhwDQC1qHaq1qGs8N4qtEKnRPANg40iq1D768DCc698SwkWp/D5tA5Mpps02Flbdy/qmVIO4wLb60v356/nJIoWNsQX2stwOrHT058xPqDJZoqPEAr3roSABxdNu0vLDaO79JUstX760CCtcN4IdD6EitnA9l05jbRHW6WNOtM7QFaG8RjC6GE/LuOBMrRs4sNhZvWOd7F2kRsJT6/bvqsILnDh4nia8dq7P11IMHbYbzQN9wsgNxhxJylmQqKtZFxmmaYwxLLapzTzPYoRrs0HfJbo3lVy8xyfpvEWT9b5tSyvVj+I0pTAQu/lbnDeO6L9cWbD4CnuplmYoWPzndf3jpo9wlRePmTE4ffanWozfoqc+4L5qma5+Hvp9vTu3E6wsXIJhNlK8G1iKDNmj8nlbAc8BB6xMgCrE7+5MTxt1qdaztAa96cMK6dRYfGpJkyCg4aa95QmjVfjpZrJtP+2qawDIeG4LFIBWX1/qqWIe8wnqg+QOvzizkK0Kb9FZiX+Cv/LICs7l/VMuQ/WKK54sFzP3/9/vzvJZwaNtmI0EvUI3YOG11WtT85cfoO46Eu6/uOOemE72Vj+aCjAqlFPK3M4RDL2iEXd1RshFGJ3tN0YK5qmWn2MtdlfXbF3KHJUnMJqDV7KMt6ldwBSiX6gljzZPDcKk7TDHW4wjtA67OLucOU0cDPUnp5HUiw7INblvX8QSmE1CqgDBeOljow14FEjbM4yr/ryK85NXUST2YDR8vCketAArHdpQ7hfvEwnOMkAhssqi5+WFcyTtOMRQhldZtbbcAYDTwtEyd/cuJmjwSZw/HAcU7ixciwS2gaRutExmmaMQmBrGHgSacfTWgUGFrupqGKvRWx3aUOlwMPQZuogOd208dPvliXAbAahTjWgBGx26AJo1XGVMCMQhhryHD7PWyrL9ZxaKwMV8ruQQ/V73/KyBPrIAATG6cOB4OYccvxQbEZvuNhaTUL/bTCL8KfBGhmFUOEpf/GE+s0ABMbZw5rT38hGyJHidF3PQy7tcSzmt71MKw/QBjWKE8EHIzW0vSuh3GA1oMZAZcp3sKPnYvNAmBlDieDMPAkF48ko+P6WhyF3Spl/bUhgjUObGwmjOnya5JXrccbPLf/ExunDleDqHAnD7tr3o3cr4a7VxZOWBvvrRPbMkd4NjMq7I+7cHEvEU91Hx4/8y+NmAcvojkVsLl157S9NWQcn2N/3zPvZIbaXV36YB0RW0ttp2mG5KycLgYxY31/e3p37WzOXckIb1V0lAK0JER8Tu5n9rs0DRrJ5xzIigBuWxuU5x01WM27KsJiRZniTiBySB/BAeuEeXlkpMLKHM4G4SFUuXw/jWefreMBSiDCah5aW+ODlzmqOpdvXVAvj5QirOah9S5NA4T/OcQPpd5aU28cXImY2DhzWA1ChH85RsRbS+rlkUaClftu1wwOdYbjmnsFmpnvVra1PCtEK0Boaw0Lu0twKKfglxdKuHVOvjxSQfUiLs2+g8zheOA/2aYz50tOxd94Ia40sFbC31y4YTcr72aDtbtWyhmjxCGH9A9+LNtasC+P1Ei9iGPezXCWlrs0DRRe5sg+Ls1bobVrSwHnrBRoKmDM38CRofUwpqwXsn6/2Ty1YsFsrXAWTvUfoJW8j8w3ykpb1/NywlDg2rWxguWRCcxUwKwWT6nD+SBYyM+peoO6EGsdaFgeqUitRcO/8epgEC2ilLO4Vm1LOWUKt0vTBnF5eMhnLcD6BGsra7wIX65T81NX5UjjaEqmMtauXM2nJZO1nHZ9CMhzovgPUUbTufheFw2/LMF43xjkXpal6Ve8JP8NBba5P6wGEUNebjX4wV1Dhr5c74Cttlw/gY2cndFczPUZULTjrN4DYnL9CDTkegK1ygZ9rk9QJ9lg3xlNz4BM58HhyR+ruQhMImx/TJrrb7C7wZjrZ3Dj3DvsHx3R7ZC0x0G6/eDB7TXZzmh6H5Dl+hQoyPUuIM/1M+DdIWmPA8odZ/U9oMv1QWDK9UYgyfWiKC7meijA7jirx1JL0FyfpaxGgLn+C1i5ngwwuT4NCLm+DWPDXG+nTvsPOUVDKM6d6wHVZWlyUIq+zx8WWXI9ozorOuQufezZI3qDybDb8lxP6Q+m5XdSw3I6Wwx6UfXCaIN7V/VP/8uf/pf/z6kMAA==";function n(t){return String(t??"").replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;").replace(/'/g,"&#39;")}function b(t){const a=String(t??"").trim(),e=a.toLowerCase();return!!a&&a!=="-"&&e!=="không áp dụng"&&e!=="không có"&&e!=="chưa cập nhật"}function l(t,a,e={}){if(e.hideIfEmpty&&!b(a))return"";const o=b(a)?a:e.fallback||"Không có";return`
    <div class="info-item${e.wide?" info-item-wide":""}">
      <span class="info-label">${n(t)}</span>
      <span class="info-value">${n(o)}</span>
    </div>
  `}function g(t,a,e,o={}){const i=o.total?"money-row money-row-total":"money-row",s=o.discount?"money-value money-value-discount":"money-value",d=o.discount?"- ":o.plus?"+ ":"",c=o.logoSrc?`<span class="money-label-with-logo"><span>${n(t)}</span><img src="${n(o.logoSrc)}" alt="${n(o.logoAlt||"")}" class="money-label-logo" /></span>`:n(t);return`
    <div class="${i}">
      <span>${c}</span>
      <span class="${s}">${n(d+e(a||0))}</span>
    </div>
  `}function f(t,a,e={}){const o=e.discount?"money-value money-value-discount":"money-value";return`
    <div class="money-row">
      <span>${n(t)}</span>
      <span class="${o}">${n(a)}</span>
    </div>
  `}function z(t){return new Intl.NumberFormat("vi-VN",{maximumFractionDigits:2}).format(Number(t||0))}function V(t,a,e){const o=Number(t?.loaiGiamGia),i=Number(t?.giaTriGiamGia||0);return o===1&&i>0?`${z(i)}% (- ${e(a||0)})`:`- ${e(a||i||0)}`}function S({invoice:t,formatCurrency:a,formatDate:e,filename:o="hoa-don",targetWindow:i=null}){if(!t)return!1;const s=i||window.open("","_blank","width=1100,height=800");if(!s)throw new Error("Trình duyệt đang chặn cửa sổ in PDF.");const d=Array.isArray(t.sanPham)?t.sanPham:[],c=d.reduce((r,u)=>r+Number(u.thanhTien||0),0),h=Number(t.phiVanChuyen||0),p=Number(t.giamGia||0),x=c+h-p,m=b(t.voucher)&&p>0,v=m?V(t,p,a):"",w=t.ngayTao?e(t.ngayTao):"Không có",y=e(new Date().toISOString()),W=t.maHoaDon||o||"SPORTSHOE",k=t.trangThai||"Chưa cập nhật",T=d.map((r,u)=>`
        <tr>
          <td class="cell-center">${u+1}</td>
          <td>
            <span class="product-name">${n(r.tenSanPham||"-")}</span>
            <span class="product-meta">${n([r.mauSac,r.kichCo].filter(Boolean).join(" / ")||r.phanLoai||"-")}</span>
          </td>
          <td class="cell-center tabular">${n(r.soLuong||0)}</td>
          <td class="cell-money tabular">${n(a(r.donGia||0))}</td>
          <td class="cell-money tabular">${n(a(r.thanhTien||0))}</td>
        </tr>
      `).join("");return s.document.open(),s.document.write(`
    <!doctype html>
    <html lang="vi">
      <head>
        <meta charset="UTF-8" />
        <title>${n(o)}</title>
        <style>
          :root {
            --brand: #B82220;
            --brand-dark: #8f1716;
            --ink: #0f172a;
            --muted: #64748b;
            --line: #e2e8f0;
            --soft: #f8fafc;
            --soft-red: #fff1f2;
          }

          * {
            box-sizing: border-box;
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
          }

          body {
            margin: 0;
            background: #f1f5f9;
            color: var(--ink);
            font-family: "Inter", "Be Vietnam Pro", "Segoe UI", Arial, sans-serif;
            font-size: 13px;
            line-height: 1.45;
          }

          .invoice-page {
            width: 210mm;
            min-height: 297mm;
            margin: 0 auto;
            background: #ffffff;
            padding: 18mm;
          }

          .invoice-card {
            overflow: hidden;
            border: 1px solid #fecaca;
            border-radius: 20px;
            background: #ffffff;
            box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
          }

          .hero {
            border: 0;
            border-radius: 0;
            overflow: hidden;
            margin-bottom: 0;
          }

          .hero-top {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            background: var(--brand);
            color: #ffffff;
            padding: 14px 18px;
          }

          .brand {
            display: flex;
            align-items: center;
            gap: 0;
          }

          .brand-name {
            margin: 0;
            font-size: 18px;
            font-weight: 850;
          }

          .brand-subtitle {
            margin: 3px 0 0;
            color: #fee2e2;
            font-size: 11px;
          }

          .hero-code {
            text-align: right;
          }

          .hero-code-label {
            display: block;
            color: #fee2e2;
            font-size: 10px;
            font-weight: 700;
            text-transform: uppercase;
          }

          .hero-code-value {
            display: block;
            margin-top: 4px;
            font-size: 15px;
            font-weight: 500;
            letter-spacing: 0;
          }

          .hero-body {
            display: grid;
            grid-template-columns: 1.3fr 0.7fr;
            gap: 12px;
            background: #fff7f7;
            padding: 13px 18px;
          }

          .invoice-title {
            margin: 0;
            font-size: 26px;
            line-height: 1.1;
            font-weight: 900;
            color: var(--brand-dark);
          }

          .printed-at {
            margin: 8px 0 0;
            color: var(--muted);
            font-size: 12px;
          }

          .status-pill {
            justify-self: end;
            align-self: start;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            border-radius: 999px;
            background: #ffffff;
            border: 1px solid #fecaca;
            padding: 7px 11px;
            color: var(--brand);
            font-weight: 500;
          }

          .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: var(--brand);
          }

          .section {
            border: 0;
            border-top: 1px solid var(--line);
            border-radius: 0;
            padding: 18px;
            margin-bottom: 0;
            break-inside: avoid;
          }

          .section-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            margin-bottom: 12px;
            padding-bottom: 10px;
            border-bottom: 1px solid var(--line);
          }

          .section-title {
            margin: 0;
            color: var(--ink);
            font-size: 15px;
            font-weight: 850;
          }

          .section-note {
            color: var(--muted);
            font-size: 12px;
            font-weight: 500;
          }

          .info-grid {
            display: grid;
            grid-template-columns: minmax(0, 1fr);
            gap: 8px;
          }

          .info-item {
            display: grid;
            grid-template-columns: 128px minmax(0, 1fr);
            gap: 14px;
            align-items: start;
            min-height: 26px;
          }

          .info-item-wide {
            grid-column: 1 / -1;
          }

          .info-label {
            color: var(--muted);
            font-weight: 650;
          }

          .info-value {
            color: var(--ink);
            font-weight: 500;
            word-break: break-word;
          }

          table {
            width: 100%;
            table-layout: fixed;
            border-collapse: separate;
            border-spacing: 0;
            overflow: hidden;
            border: 1px solid var(--line);
            border-radius: 14px;
            font-size: 12px;
          }

          th,
          td {
            padding: 11px 12px;
            text-align: left;
            vertical-align: top;
            border-bottom: 1px solid var(--line);
          }

          th {
            background: var(--soft-red);
            color: var(--brand-dark);
            font-size: 11px;
            font-weight: 850;
          }

          th:nth-child(1),
          th:nth-child(3) {
            text-align: center;
          }

          th:nth-child(4),
          th:nth-child(5) {
            text-align: right;
          }

          tbody tr:nth-child(even) td {
            background: var(--soft);
          }

          tbody tr:last-child td {
            border-bottom: 0;
          }

          .product-name {
            display: block;
            color: var(--ink);
            font-weight: 500;
          }

          .product-meta {
            display: block;
            margin-top: 3px;
            color: var(--muted);
            font-size: 11px;
          }

          .cell-type {
            color: var(--muted);
          }

          .cell-center {
            text-align: center;
          }

          .cell-money {
            text-align: right;
            white-space: nowrap;
          }

          .tabular {
            font-variant-numeric: tabular-nums;
          }

          .summary-layout {
            display: block;
            border-top: 1px solid var(--line);
            padding: 18px;
            break-inside: avoid;
          }

          .thanks {
            margin-top: 16px;
            border-radius: 0;
            background: transparent;
            border: 0;
            border-top: 1px solid var(--line);
            padding: 14px 0 0;
            color: var(--muted);
          }

          .thanks strong {
            display: block;
            margin-bottom: 6px;
            color: var(--ink);
            font-size: 15px;
          }

          .summary-card {
            width: 100%;
            margin-left: 0;
            border-radius: 0;
            border: 0;
            background: transparent;
            padding: 0;
          }

          .money-row {
            display: flex;
            justify-content: space-between;
            gap: 14px;
            padding: 8px 0;
            color: var(--muted);
          }

          .money-label-with-logo {
            display: inline-flex;
            align-items: center;
            gap: 7px;
          }

          .money-label-logo {
            height: 15px;
            width: auto;
            object-fit: contain;
          }

          .money-value {
            color: var(--ink);
            font-weight: 500;
            font-variant-numeric: tabular-nums;
            white-space: nowrap;
          }

          .money-value-discount {
            color: #059669;
          }

          .money-row-total {
            margin-top: 8px;
            padding-top: 14px;
            border-top: 1px solid #fecaca;
            color: var(--brand);
            font-size: 16px;
            font-weight: 900;
          }

          .money-row-total .money-value {
            color: var(--brand);
            font-size: 18px;
          }

          @media print {
            body {
              background: #ffffff;
            }

            .invoice-page {
              width: auto;
              min-height: auto;
              margin: 0;
              padding: 0;
            }

            .invoice-card {
              box-shadow: none;
            }
          }

          @page {
            size: A4;
            margin: 12mm;
          }
        </style>
      </head>
      <body>
        <main class="invoice-page">
          <article class="invoice-card">
          <section class="hero">
            <div class="hero-top">
              <div class="brand">
                <div>
                  <p class="brand-name">SportShoe</p>
                  <p class="brand-subtitle">Giày thể thao chính hãng</p>
                </div>
              </div>
              <div class="hero-code">
                <span class="hero-code-label">Mã hóa đơn</span>
                <span class="hero-code-value">${n(W)}</span>
              </div>
            </div>
            <div class="hero-body">
              <div>
                <h1 class="invoice-title">Hóa đơn bán hàng</h1>
                <p class="printed-at">Ngày tạo: ${n(w)} · In lúc: ${n(y)}</p>
              </div>
              <div class="status-pill">
                <span class="status-dot"></span>
                ${n(k)}
              </div>
            </div>
          </section>

          <section class="section">
            <div class="section-header">
              <h2 class="section-title">Thông tin hóa đơn</h2>
              <span class="section-note">${n(t.loaiDon||"Chưa cập nhật")}</span>
            </div>
            <div class="info-grid">
              ${l("Nhân viên",t.maNhanVien||t.tenNhanVien||"Chưa gán")}
              ${l("Khách hàng",t.tenKhachHang,{hideIfEmpty:!0})}
              ${l("Email",t.email,{hideIfEmpty:!0})}
              ${l("Số điện thoại",t.soDienThoai,{hideIfEmpty:!0})}
              ${l("Địa chỉ",t.diaChi,{wide:!0,fallback:"Mua tại quầy"})}
              ${l("Ghi chú",t.ghiChu,{wide:!0,hideIfEmpty:!0})}
            </div>
          </section>

          <section class="section">
            <div class="section-header">
              <h2 class="section-title">Danh sách sản phẩm</h2>
              <span class="section-note">${d.length} sản phẩm</span>
            </div>
            <table>
              <colgroup>
                <col style="width: 48px" />
                <col />
                <col style="width: 96px" />
                <col style="width: 132px" />
                <col style="width: 142px" />
              </colgroup>
              <thead>
                <tr>
                  <th>STT</th>
                  <th>Sản phẩm</th>
                  <th>Số lượng</th>
                  <th>Đơn giá</th>
                  <th>Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                ${T||'<tr><td colspan="5" class="cell-center">Không có sản phẩm</td></tr>'}
              </tbody>
            </table>
          </section>

          <section class="summary-layout">
            <aside class="summary-card">
              <div class="section-header">
                <h2 class="section-title">Tổng kết thanh toán</h2>
              </div>
              ${g("Tổng tiền hàng",c,a)}
              ${h>0?g("Phí vận chuyển",h,a,{plus:!0,logoSrc:q,logoAlt:"GHN"}):""}
              ${m?f("Mã giảm giá",t.voucher):""}
              ${m?f("Giá trị giảm",v,{discount:!0}):""}
              ${g("Tổng thanh toán",x,a,{total:!0})}
            </aside>

            <div class="thanks">
              <strong>Cảm ơn quý khách!</strong>
              Hóa đơn được phát hành bởi SportShoe. Vui lòng kiểm tra thông tin sản phẩm và tổng thanh toán trước khi rời quầy.
            </div>
          </section>
          </article>
        </main>

        <script>
          window.onload = () => {
            setTimeout(() => {
              window.focus();
              window.print();
            }, 200);
          };
          window.onafterprint = () => window.close();
        <\/script>
      </body>
    </html>
  `),s.document.close(),!0}export{q as l,S as p};
