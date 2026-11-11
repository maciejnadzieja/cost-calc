export type PaginationItem = number | 'ellipsis'

export function paginationWindow(currentPage: number, totalPages: number): PaginationItem[] {
  if (!Number.isInteger(totalPages) || totalPages < 1) return []

  const lastPage = totalPages - 1
  const current = Number.isInteger(currentPage) ? Math.max(0, Math.min(currentPage, lastPage)) : 0
  const pages = new Set<number>([0, lastPage])

  if (totalPages <= 7) {
    for (let page = 0; page < totalPages; page++) pages.add(page)
  } else {
    for (let page = Math.max(0, current - 2); page <= Math.min(lastPage, current + 2); page++) {
      pages.add(page)
    }
  }

  const visible: PaginationItem[] = []
  let previous = -1
  for (const page of [...pages].sort((a, b) => a - b)) {
    if (previous >= 0 && page - previous === 2) visible.push(previous + 2)
    else if (previous >= 0 && page - previous > 2) visible.push('ellipsis')
    visible.push(page + 1)
    previous = page
  }
  return visible
}
