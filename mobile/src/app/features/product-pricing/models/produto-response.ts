export interface ProdutoResponse {
  id: number;
  codigoBarras: string;
  descricao: string;
  unidade: string;
  precoVenda: number;
  precoCusto: number | null;
  codigoNcm: string;
  estoqueAtual: number | null;
}

