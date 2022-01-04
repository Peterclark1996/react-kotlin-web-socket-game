import { v4 as uuid } from 'uuid'

export const generateHtmlId = () => `a${uuid().replace(/-/g, "")}`