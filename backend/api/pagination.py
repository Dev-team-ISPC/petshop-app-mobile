from rest_framework.pagination import PageNumberPagination


class PaginacionEstandar(PageNumberPagination):
    """
    20 por página. La app puede pedir otro tamaño con ?page_size=, útil
    para los selectores que necesitan el catálogo completo de una.
    """

    page_size = 20
    page_size_query_param = 'page_size'
    max_page_size = 100
