import { useEffect, useRef } from 'react'
import * as echarts from 'echarts'

type ChartDatum = {
  nome: string
  total: number
}

type ChartCardProps = {
  title: string
  data: ChartDatum[]
  type: 'bar' | 'pie'
}

export function ChartCard({ title, data, type }: ChartCardProps) {
  const ref = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    if (!ref.current) return

    const chart = echarts.init(ref.current)
    const names = data.map((item) => item.nome)
    const totals = data.map((item) => item.total)

    chart.setOption(type === 'pie'
      ? {
          backgroundColor: 'transparent',
          tooltip: { trigger: 'item' },
          legend: { bottom: 0, textStyle: { color: '#c7d2fe' } },
          series: [
            {
              type: 'pie',
              radius: ['40%', '70%'],
              itemStyle: { borderRadius: 10, borderColor: '#081225', borderWidth: 3 },
              label: { color: '#e2e8f0' },
              data: data.map((item) => ({ name: item.nome, value: item.total })),
            },
          ],
        }
      : {
          backgroundColor: 'transparent',
          tooltip: { trigger: 'axis' },
          grid: { left: 24, right: 12, top: 20, bottom: 36, containLabel: true },
          xAxis: {
            type: 'category',
            data: names,
            axisLabel: { color: '#c7d2fe', interval: 0, rotate: names.length > 4 ? 20 : 0 },
            axisLine: { lineStyle: { color: '#334155' } },
          },
          yAxis: {
            type: 'value',
            axisLabel: { color: '#c7d2fe' },
            splitLine: { lineStyle: { color: 'rgba(148, 163, 184, 0.15)' } },
          },
          series: [
            {
              type: 'bar',
              data: totals,
              barWidth: 28,
              itemStyle: {
                borderRadius: [10, 10, 0, 0],
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#38bdf8' },
                  { offset: 1, color: '#0ea5e9' },
                ]),
              },
            },
          ],
        })

    const handleResize = () => chart.resize()
    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
      chart.dispose()
    }
  }, [data, type])

  return (
    <section className="panel chart-panel">
      <div className="panel-head">
        <h3>{title}</h3>
      </div>
      <div ref={ref} className="chart-canvas" />
    </section>
  )
}
