export interface EntradaEstoqueRequest {
  quantidade: number;
  precoCusto: number;
  /** Formato yyyy-MM-dd. Omitido quando o usuário não informa a data. */
  dataVencimento?: string;
}

export interface EntradaEstoqueResponse {
  produtoId: number;
  estoqueAnterior: number;
  estoqueAtual: number;
  precoCustoAnterior: number | null;
  precoCustoAtual: number;
}
