/**
 * MODULE 18: Charts — Canvas-based performance visualization.
 * Lightweight alternative to Chart.js — zero dependencies.
 */

window.CL_Charts = {

  drawPerformanceChart(canvasId) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx    = canvas.getContext("2d");
    const W      = canvas.parentElement.clientWidth;
    const H      = canvas.parentElement.clientHeight || 160;
    canvas.width  = W;
    canvas.height = H;

    // Generate mock portfolio data (last 30 days)
    const days   = 30;
    const data   = [];
    let   value  = 120_000;
    for (let i = 0; i < days; i++) {
      value += (Math.random() - 0.35) * 2000;
      data.push(value);
    }

    const min    = Math.min(...data);
    const max    = Math.max(...data);
    const range  = max - min || 1;
    const pad    = { t: 12, r: 8, b: 24, l: 48 };
    const cW     = W - pad.l - pad.r;
    const cH     = H - pad.t - pad.b;
    const stepX  = cW / (days - 1);

    const toX = (i) => pad.l + i * stepX;
    const toY = (v) => pad.t + cH - ((v - min) / range) * cH;

    // Background
    ctx.fillStyle = "#121212";
    ctx.fillRect(0, 0, W, H);

    // Grid lines
    ctx.strokeStyle = "#2A2A2A";
    ctx.lineWidth   = 1;
    for (let g = 0; g <= 4; g++) {
      const y = pad.t + (cH / 4) * g;
      ctx.beginPath(); ctx.moveTo(pad.l, y); ctx.lineTo(W - pad.r, y); ctx.stroke();
      const label = ((max - (range / 4) * g) / 1000).toFixed(0) + "k";
      ctx.fillStyle   = "#9E9E9E";
      ctx.font        = "10px sans-serif";
      ctx.textAlign   = "right";
      ctx.fillText("$" + label, pad.l - 4, y + 3);
    }

    // Gradient fill
    const grad = ctx.createLinearGradient(0, pad.t, 0, H - pad.b);
    grad.addColorStop(0,   "rgba(184,115,51,0.35)");
    grad.addColorStop(0.6, "rgba(184,115,51,0.08)");
    grad.addColorStop(1,   "rgba(184,115,51,0.00)");

    ctx.beginPath();
    ctx.moveTo(toX(0), H - pad.b);
    for (let i = 0; i < days; i++) ctx.lineTo(toX(i), toY(data[i]));
    ctx.lineTo(toX(days - 1), H - pad.b);
    ctx.closePath();
    ctx.fillStyle = grad;
    ctx.fill();

    // Line
    ctx.beginPath();
    ctx.strokeStyle = "#B87333";
    ctx.lineWidth   = 2.5;
    ctx.lineJoin    = "round";
    for (let i = 0; i < days; i++) {
      i === 0 ? ctx.moveTo(toX(i), toY(data[i])) : ctx.lineTo(toX(i), toY(data[i]));
    }
    ctx.stroke();

    // Last point dot
    const lx = toX(days - 1);
    const ly = toY(data[days - 1]);
    ctx.beginPath();
    ctx.arc(lx, ly, 4, 0, Math.PI * 2);
    ctx.fillStyle = "#B87333";
    ctx.fill();

    // X-axis labels
    ctx.fillStyle = "#9E9E9E";
    ctx.font      = "10px sans-serif";
    ctx.textAlign = "center";
    [0, 6, 13, 20, 29].forEach(i => {
      const label = `D-${days - 1 - i}`;
      ctx.fillText(label, toX(i), H - 4);
    });
  },

};
